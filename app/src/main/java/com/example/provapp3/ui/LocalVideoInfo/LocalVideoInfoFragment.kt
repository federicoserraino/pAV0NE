package com.example.provapp3.ui.LocalVideoInfo

import android.content.Intent
import android.graphics.Color
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.provapp3.DataClasses.LocalVideoInfo
import com.example.provapp3.R
import com.example.provapp3.RoomDB.VideoEntity
import com.example.provapp3.RoomDB.VideoRepository
import com.example.provapp3.ui.ShowVideo.ShowVideoActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_local_video_info.*


class LocalVideoInfoFragment : Fragment() {

    private val str_undefined = "Undefined"
    private val viewModel:LocalVideoInfoVM by viewModels{
        LocalVideoInfoVM.InfoVideoViewModelFactory(VideoRepository(requireActivity().application))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_local_video_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState == null){
            val title = arguments?.getString("video_title") ?: "No Title"
            val size = arguments?.getLong("video_size") ?: 0
            val uri = arguments?.getString("video_uri") ?: "No URI"
            viewModel.setVideo(uri, title, size)
            extractVideoInfo()
        }

        viewModel.getVideo().observe(viewLifecycleOwner, Observer { videoEntity ->
            updateVideoEntityView(videoEntity)
        })

        viewModel.getVideoInfo().observe(viewLifecycleOwner, Observer { videoInfo ->
            updateInfoVideoViews(videoInfo)
        })

        bn_edit.setOnClickListener {
            val old_title = title_view.text.toString()
            bn_edit.visibility = View.GONE
            bn_save.visibility = View.VISIBLE
            title_desc.visibility = View.GONE
            title_view.visibility = View.GONE
            title_edit_desc_view.visibility = View.VISIBLE
            title_edit_view.setText(old_title)
            setHasOptionsMenu(true)
        }

        bn_save.setOnClickListener {
            saveNewTitle()
        }

        FAB_play.setOnClickListener {
            val video_uri = viewModel.getVideoURI()
            if(checkVideoIntegrity(video_uri)){
                /* PLAYBACK TYPE:
                      *) 0 -> Local video
                      *) 1 -> Dash Media
                  */
                val intent = Intent(requireContext(), ShowVideoActivity::class.java)
                intent.putExtra("playback_type",0)
                intent.putExtra("local_video_uri",video_uri)
                startActivity(intent)
            }
        }

        

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.save_button ->{
                saveNewTitle()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNewTitle(){
        val new_title = title_edit_view.text.toString()
        bn_save.visibility = View.GONE
        bn_edit.visibility = View.VISIBLE
        title_edit_desc_view.visibility = View.GONE
        title_desc.visibility = View.VISIBLE
        title_view.visibility = View.VISIBLE
        viewModel.updateVideoTitle(new_title)
        setHasOptionsMenu(false)
    }

    private fun extractVideoInfo(){
        val extractor = MediaExtractor()
        try{
            extractor.setDataSource(requireContext(), Uri.parse(viewModel.getVideoURI()),null)
            var i = 0
            while(i < extractor.trackCount){
                val trackformat = extractor.getTrackFormat(i)
                //Log.d("III",trackformat.toString())
                val mimetype = trackformat.getString(MediaFormat.KEY_MIME) ?: "No type"
                if(mimetype.startsWith("video")){
                    val codec = mimetype.substringAfter('/')
                    var width = 0 ; var height = 0 ; var duration:Long = 0 ; var framerate = 0 ; var bitrate = 0
                    if(trackformat.containsKey(MediaFormat.KEY_WIDTH) && trackformat.containsKey(MediaFormat.KEY_HEIGHT)){
                        width = trackformat.getInteger(MediaFormat.KEY_WIDTH)
                        height = trackformat.getInteger(MediaFormat.KEY_HEIGHT)
                    }
                    if(trackformat.containsKey(MediaFormat.KEY_DURATION))
                        duration = trackformat.getLong(MediaFormat.KEY_DURATION) / 1000000
                    if(trackformat.containsKey(MediaFormat.KEY_BIT_RATE))
                        bitrate = trackformat.getInteger(MediaFormat.KEY_BIT_RATE)
                    if(trackformat.containsKey(MediaFormat.KEY_FRAME_RATE))
                        framerate = trackformat.getInteger(MediaFormat.KEY_FRAME_RATE)
                    viewModel.setVideoInfo(codec,width,height,duration,bitrate,framerate)
                    break
                }
                i++
            }
        } catch (e:Exception){
            // Show negative Snackbar
            Snackbar.make(requireView(),"Unable to show video info",Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackbar_negative))
                .setTextColor(Color.WHITE)
                .show()
        }
        extractor.release()
    }

    private fun getBitFormat(size:Float,divider:Int):String{
        var dim = size / divider
        var dim_unit = "KB"
        if(dim > divider){
            dim /= divider
            if(dim < divider)
                dim_unit = "MB"
            else {
                dim /= divider
                dim_unit = "GB"
            }
        }
       return String.format("%.1f %s",dim,dim_unit)
    }

    private fun updateVideoEntityView(video:VideoEntity){
        title_view.text =
            if(video.title == "") "No Title"
            else video.title

        Glide.with(requireContext())
            .load(video.uri)
            .error(R.drawable.logo_pavone)
            .into(preview)

        val size = video.size
        val str =
            if(size == 0.toLong())
                str_undefined
            else
                getBitFormat(size.toFloat(),1024)
        dimensione_view.text = str
    }

    private fun updateInfoVideoViews(videoinfo: LocalVideoInfo){
        videocodec_view.text = videoinfo.codec

        var str:String =
            if(videoinfo.width == 0 || videoinfo.height == 0)
                str_undefined
            else
                "${videoinfo.width} x ${videoinfo.height}"
        width_x_height_view.text = str

        var sec = videoinfo.duration
        if(sec > 0){
            val dur:String
           if(sec < 60)
               dur = "${sec}s"
            else{
               var min = sec/60
               if(min < 60){
                   sec %= 60
                   dur = "${min}m ${sec}s"
               } else {
                   val h = min / 60
                   min %= 60
                   dur = "${h}h ${min}m"
               }
           }
            str = dur
        } else
            str = str_undefined

        durata_view.text = str

        val bitrate = videoinfo.bitrate.toFloat()
        str =
            if(bitrate > 0)
                getBitFormat(bitrate,1000) + "/s"
            else
                str_undefined
        bitrate_view.text = str

        str =
            if(videoinfo.framerate ==0)
                str_undefined
            else
                "${videoinfo.framerate} Hz"
        framerate_view.text = str
    }

    private fun checkVideoIntegrity(video_uri:String):Boolean{
        val extractor = MediaExtractor()
        var res = true
        try{
            extractor.setDataSource(requireContext(),Uri.parse(video_uri),null)
        } catch (e:Exception){
            res = false
            // Show negative Snackbar
            Snackbar.make(requireView(),"Unable to play video, format not supported or it has been deleted from device!",Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackbar_negative))
                .setTextColor(Color.WHITE)
                .show()
        }
        extractor.release()
        return res
    }


}