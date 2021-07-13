package com.example.provapp3.ui.LocalVideos

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaExtractor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.provapp3.R
import com.example.provapp3.RoomDB.VideoEntity
import com.example.provapp3.RoomDB.VideoRepository
import com.example.provapp3.ui.ShowVideo.ShowVideoActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_local_vodeos.*

class LocalVideosFragment : Fragment() {

    private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100
    private val LAUNCH_ACTION_PICK = 200

    private val adapter:LocalVideosAdapter by lazy {LocalVideosAdapter()}
    private val viewModel: LocalVideosVM by viewModels {
        LocalVideosVM.LocalVideoViewModelFactory(VideoRepository(requireActivity().application))
    }

    private fun pickVideoUri(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, LAUNCH_ACTION_PICK)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_local_vodeos,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val span_count =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3
            else 2

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
                GridLayoutManager(context, span_count, GridLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        FAB.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            } else {
                // Permesso già concesso
                pickVideoUri()
            }
        }

        adapter.getSize().observe(viewLifecycleOwner, Observer { size ->
            var visibility = View.VISIBLE
            if (size > 0)
                visibility = View.GONE
            emptyMessage.visibility = visibility
        })

        adapter.setOnVideoClickListener(object : LocalVideosAdapter.ClickListener {

            override fun onVideoClick(video: VideoEntity) {
                if(checkVideoIntegrity(video)){
                    /* PLAYBACK TYPE:
                        *) 0 -> Local video
                        *) 1 -> Dash Media
                    */
                    val intent = Intent(requireContext(), ShowVideoActivity::class.java)
                    intent.putExtra("playback_type",0)
                    intent.putExtra("local_video_uri",video.uri)
                    startActivity(intent)
                }
            }

            override fun onInfoClick(video: VideoEntity) {
                if(checkVideoIntegrity(video)){
                    val b = Bundle()
                    b.putString("video_uri",video.uri)
                    b.putString("video_title",video.title)
                    b.putLong("video_size",video.size)
                    findNavController().navigate(R.id.action_navigation_local_to_local_video_info,b)
                }
            }

            override fun onRenameClick(video: VideoEntity) {
                val alert_view = layoutInflater.inflate(R.layout.alertdialog_rename_video,null)
                val editText = alert_view
                    .findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.alert_editText)
                editText.setText(video.title)
                AlertDialog.Builder(context)
                    .setView(alert_view)
                    .setPositiveButton("SAVE") { _, _ ->
                        val new_title = editText.text.toString()
                        video.title =
                            if(new_title == "") "No Title"
                            else new_title
                        viewModel.updateVideo(video)
                    }
                    .setNeutralButton("CANCEL"){_,_ ->
                    }
                    .show()
            }

            override fun onRemoveClick(video: VideoEntity) {
                viewModel.removeVideo(video)
            }

        })

        viewModel.getAllVideos().observe(viewLifecycleOwner, Observer { list ->
            adapter.setVideos(list)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permesso concesso, yay!
                pickVideoUri()
            } else {
                // Permesso negato, boo!
                Toast.makeText(requireActivity(),"Permission is needed to choose a video!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Ricezione URI video
        if(resultCode == Activity.RESULT_OK && requestCode == LAUNCH_ACTION_PICK) {
            val uri = data?.data
            var title = "No Title"
            var size:Long = 0
            try{
                val cursor = requireContext().contentResolver.query(uri!!,null,null,null,null)
                val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                title = cursor.getString(nameIndex)
                size = cursor.getLong(sizeIndex)
                cursor.close()
            } catch(exc:Exception){
                Log.e("ERRORE","Impossibile recuperare nome completo",exc)
                if(uri != null)
                    title = uri.lastPathSegment ?: "No Title"
            }
            val video = VideoEntity(uri.toString(),title,size)
            viewModel.addVideo(video)
            // Show positive Snackbar
            Snackbar.make(requireView(),"Video has been added",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackbar_positive))
                .setTextColor(Color.WHITE)
                .show()
        }
    }

    private fun checkVideoIntegrity(video:VideoEntity):Boolean{
        val extractor = MediaExtractor()
        var res = true
        try{
            extractor.setDataSource(requireContext(),Uri.parse(video.uri),null)
        } catch (e:Exception){
            res = false
           // viewModel.removeVideo(video)
            // Show negative Snackbar
            Snackbar.make(requireView(),"Unable to open this video, format not supported or it has been deleted from device!",Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackbar_negative))
                .setTextColor(Color.WHITE)
                .show()
        }
        extractor.release()
        return res
    }

}