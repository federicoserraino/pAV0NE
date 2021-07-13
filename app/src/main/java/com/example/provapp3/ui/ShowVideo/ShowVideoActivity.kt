package com.example.provapp3.ui.ShowVideo

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.provapp3.DataClasses.PlayerTrack
import com.example.provapp3.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.activity_show_video.*

class ShowVideoActivity : AppCompatActivity() {

    private val viewModel:ShowVideoVM by viewModels()
    private lateinit var exoPlayer: SimpleExoPlayer
    private val trackSelector by lazy { DefaultTrackSelector(this) }
    private val bn_arrow_back: ImageView by lazy {player_view.findViewById(R.id.bn_go_back) as ImageView }
    private val bn_full_screen: ImageView by lazy {player_view.findViewById(R.id.bn_full_screen) as ImageView }
    private val media_current_quality by lazy {player_view.findViewById<TextView>(R.id.media_quality_textview) }
    private val bn_select_quality by lazy { player_view.findViewById<LinearLayout>(R.id.media_quality_layout) }
    private val bn_loop_playback by lazy { player_view.findViewById<ImageView>(R.id.bn_repeat_media) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_video)

        if (savedInstanceState == null) {
            val videoURI =
                when {
                    intent.action == Intent.ACTION_SEND -> {
                        // Activity invocato esternamente tramite "share" intent
                        val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                        uri?.toString() ?: "No Uri"
                    }
                    intent.type?.startsWith("video/") == true -> {
                        // Activity invocato esternamente tramite "play with" intent
                        intent?.data?.toString() ?: "No Uri"
                    }
                    else -> {
                        /* Intent invocato dall'applicazione stessa
                            PLAYBACK TYPE:
                                *) 0 -> Local video
                                *) 1 -> Dash Media
                         */
                        when(intent?.getIntExtra("playback_type",-1)){
                            0 -> intent.getStringExtra("local_video_uri") ?: "No Uri"
                            1 -> {
                                viewModel.setDashMedia()
                                intent.getStringExtra("dash_manifest_uri") ?: "No Uri"
                            }
                            else -> "No Uri"
                        }
                    }
                }
            viewModel.setVideoURI(videoURI)
        }

        setScreenImmersiveMode()

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewModel.setOrientationLandscape(true)
            bn_full_screen.setImageResource(R.drawable.ic_fullscreen_close)
        } else {
            viewModel.setOrientationLandscape(false)
            bn_full_screen.setImageResource(R.drawable.ic_fullscreen_open)
        }

        viewModel.getLiveResolution().observe(this, Observer {
            media_current_quality.text = it
        })

        bn_arrow_back.setOnClickListener {
            finish()
        }

        bn_full_screen.setOnClickListener {
            if(viewModel.isOrientationLandscape()){
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                bn_full_screen.setImageResource(R.drawable.ic_fullscreen_open)
                viewModel.setOrientationLandscape(false)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                bn_full_screen.setImageResource(R.drawable.ic_fullscreen_close)
                viewModel.setOrientationLandscape(true)
            }
        }

        bn_select_quality.setOnClickListener {
            if(viewModel.isDashMedia()){
                val alert_select_quality_view = layoutInflater.inflate(R.layout.alertdialog_quality_selection,null)
                val radio_group = alert_select_quality_view.findViewById<RadioGroup>(R.id.quality_radio_group)

                for(resolution in viewModel.getTracksResolution()){
                    val rb = RadioButton(this)
                    rb.text = resolution; rb.textSize = 20f
                    rb.setPadding(20,20,20,20)
                    radio_group.addView(rb)
                    if(viewModel.isCurrentResolution(resolution))
                        radio_group.check(rb.id)
                }

                AlertDialog.Builder(this)
                    .setView(alert_select_quality_view)
                    .setPositiveButton("OK") { _, _ ->
                        val rb = alert_select_quality_view.findViewById<RadioButton>(radio_group.checkedRadioButtonId)
                        changeTrack(rb.text.toString())
                        setScreenImmersiveMode()
                    }
                    .setNeutralButton("CANCEL") { _, _ ->
                        setScreenImmersiveMode()
                    }
                    .show()

            } else
                Toast.makeText(this,
                    "Video resolution: ${viewModel.getTrackFullResolution()}",Toast.LENGTH_SHORT).show()
        }

        bn_loop_playback.setOnClickListener {
            val is_loopback = viewModel.change_loopback_mode()
            val image_sorce =
                if(is_loopback)
                    R.drawable.ic_repeat_on
                else
                    R.drawable.ic_repeat_off
            bn_loop_playback.setImageResource(image_sorce)
        }

        initPlayer()
        startPlayer()
    }

    @Suppress("DEPRECATION")
    private fun setScreenImmersiveMode(){
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun initPlayer(){
        val renderersFactory = DefaultRenderersFactory(this)
        /*  Utilizzo di RenderMode:
              *) EXTENSION_RENDERER_MODE_ON --> consente l'utilizzo di Libgav1VideoRenderer
                         nel caso in cui il dispositivo non presenta ulteriori decoder AV1

              *) EXTENSION_RENDERER_MODE_PREFER --> assegna priorità all'utilizzo di Libgav1VideoRenderer
                         per la decodifica di sequenze video AV1
         */
        //val renderMode = EXTENSION_RENDERER_MODE_ON
        val renderMode = EXTENSION_RENDERER_MODE_PREFER
        renderersFactory.setExtensionRendererMode(renderMode)

        exoPlayer = SimpleExoPlayer.Builder(this,renderersFactory)
            .setTrackSelector(trackSelector)
            .build()

        val mediaItem = MediaItem.fromUri(Uri.parse(viewModel.getVideoURI()))
        exoPlayer.setMediaItem(mediaItem)

        exoPlayer.addListener(object:Player.EventListener{
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when(state){
                    Player.STATE_BUFFERING -> {
                        progess_bar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        progess_bar.visibility = View.GONE
                        if(!viewModel.isMapTraksInit())
                            inspectTracks()
                    }
                    Player.STATE_ENDED -> {
                        if(viewModel.isLoopPlayback()){
                            exoPlayer.seekTo(1)
                            //exoPlayer.playWhenReady = true
                        } else
                            finish()
                    }
                    else -> return
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                if(error.type == ExoPlaybackException.TYPE_SOURCE){
                    // Show negative Toast
                    val error_msg =
                    if(viewModel.isDashMedia()){
                        "Sorry, unable to play video!"
                    } else {
                        "Sorry, unable to play video, format not supported!"
                    }
                    Toast.makeText(this@ShowVideoActivity,error_msg,Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }

    private fun startPlayer(){
        player_view.player = exoPlayer
        player_view.keepScreenOn = true
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun inspectTracks(){
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return

        for(rendererIndex in 0 until mappedTrackInfo.rendererCount){
            val rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
            if (rendererTrackGroups.length > 0) {
                for(groupIndex in 0 until rendererTrackGroups.length){
                    val trackGroup = rendererTrackGroups.get(groupIndex)
                    for(trackIndex in 0 until trackGroup.length){
                        val format = trackGroup.getFormat(trackIndex)
                        val format_support = mappedTrackInfo.getTrackSupport(rendererIndex,groupIndex,trackIndex)
                        if(format.sampleMimeType?.startsWith("video/") == true && format_support == C.FORMAT_HANDLED){
                            val w = format.width ; val h = format.height ; val br = format.bitrate
                            viewModel.addTrack(PlayerTrack(w,h,br))
                            if(!viewModel.isDashMedia())
                                viewModel.setCurrentResolution(minOf(w,h).toString() + "p")
                        }
                    }
                }
            }
        }
        viewModel.setMapTracksInit()
    }

    private fun changeTrack(resolution:String){
        if (resolution == "Auto"){
            trackSelector.setParameters(trackSelector.buildUponParameters().clearVideoSizeConstraints())
        } else {
            val track = viewModel.getTrack(resolution)
            if(track != null)
                trackSelector.setParameters(trackSelector.buildUponParameters()
                    .setMaxVideoSize(track.width,track.height))
        }
        viewModel.setCurrentResolution(resolution)
    }


    override fun onPause() {
        super.onPause()
        exoPlayer.playbackState
        exoPlayer.playWhenReady = false
    }

    override fun onRestart() {
        super.onRestart()
        exoPlayer.playbackState
        exoPlayer.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}