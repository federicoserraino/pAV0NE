package com.example.provapp3.ui.OnlineVideos

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.provapp3.DataClasses.DashServer
import com.example.provapp3.R
import com.example.provapp3.ui.ShowVideo.ShowVideoActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_online_dash_videos.*

class DashVideosFragment : Fragment() {

    private lateinit var adapter: DashVideosAdapter
    private val example_media_dash_file = "example_dash_media.json"
    private val viewModel:DashVideosVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_online_dash_videos,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMediaURI().observe(viewLifecycleOwner, Observer {
            manifest_editText.setText(it)
        })

        initAdapter()
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter

        adapter.setOnMediaClickListener(object : DashVideosAdapter.ClickListener{
            override fun onMediaClick(manifest_uri: String) {
                startPlayback(manifest_uri)
            }
        })

        bn_paste.setOnClickListener {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text_to_paste = clipboardManager.primaryClip?.getItemAt(0)?.text ?: ""
            viewModel.setMediaURI(text_to_paste.toString())
        }

        bn_play.setOnClickListener {
            val dash_manifest_uri = manifest_editText.text.toString()
            if(dash_manifest_uri != "")
                startPlayback(dash_manifest_uri)
        }
    }

    private fun initAdapter(){
        val jsonFileString = requireContext().assets
            .open(example_media_dash_file)
            .bufferedReader()
            .use { it.readText() }

        val dash_server = Gson().fromJson(jsonFileString,DashServer::class.java)
        adapter = DashVideosAdapter(dash_server.server_uri,dash_server.samples)
    }

    private fun startPlayback(dash_manifest_uri:String){
        /* PLAYBACK TYPE:
            *) 0 -> Local video
            *) 1 -> Dash Media
        */
        val intent = Intent(requireContext(), ShowVideoActivity::class.java)
        intent.putExtra("playback_type",1)
        intent.putExtra("dash_manifest_uri",dash_manifest_uri)
        startActivity(intent)
    }
}