package com.example.provapp3.ui.LocalVideos

import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.provapp3.R
import com.example.provapp3.RoomDB.VideoEntity

class LocalVideosAdapter: RecyclerView.Adapter<LocalVideosAdapter.ViewHolder>() {

    private val videos = mutableListOf<VideoEntity>()
    private val size = MutableLiveData(1)
    private lateinit var clickListener: ClickListener

    interface ClickListener {
        fun onVideoClick(video: VideoEntity)
        fun onInfoClick(video: VideoEntity)
        fun onRenameClick(video: VideoEntity)
        fun onRemoveClick(video:VideoEntity)
    }

    fun setOnVideoClickListener(listener: ClickListener) {
        clickListener = listener
    }

    fun setVideos(newList:List<VideoEntity>){
        videos.clear()
        videos.addAll(newList)
        size.value = videos.size
        notifyDataSetChanged()
    }

    override fun getItemCount() = videos.size
    fun getSize() : LiveData<Int> = size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_local_video,parent,false)
        return ViewHolder(v,clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    inner class ViewHolder(private val v: View,listener: ClickListener) : RecyclerView.ViewHolder(v) {

        private val title:TextView = v.findViewById(R.id.title)
        private val preview:ImageView = v.findViewById(R.id.preview)
        private val ic_more:ImageView = v.findViewById(R.id.ic_more)

        init {
            preview.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION)
                    listener.onVideoClick(videos[adapterPosition])
            }

            ic_more.setOnClickListener {
                val popupMenu = PopupMenu(v.context,v)
                popupMenu.inflate(R.menu.video_more_menu)
                popupMenu.setOnMenuItemClickListener {item ->
                    when (item.itemId) {
                        R.id.menu_info -> {
                            if (adapterPosition != RecyclerView.NO_POSITION)
                                listener.onInfoClick(videos[adapterPosition])
                            true
                        }
                        R.id.menu_riproduci -> {
                            if(adapterPosition != RecyclerView.NO_POSITION)
                                listener.onVideoClick(videos[adapterPosition])
                            true
                        }
                        R.id.menu_rinomina -> {
                            if (adapterPosition != RecyclerView.NO_POSITION)
                                listener.onRenameClick(videos[adapterPosition])
                            true
                        }
                        R.id.menu_rimuovi -> {
                            if(adapterPosition != RecyclerView.NO_POSITION)
                                listener.onRemoveClick(videos[adapterPosition])
                            true
                        }
                        else -> false
                    }
                }

                try{
                    val fieldMPopupMenu = PopupMenu::class.java.getDeclaredField("mPopup")
                     fieldMPopupMenu.isAccessible = true
                    val mPopupMenu = fieldMPopupMenu.get(popupMenu)
                    mPopupMenu.javaClass
                        .getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                        .invoke(mPopupMenu,true)
                } catch(e:Exception){
                    Log.e("ERROR","Impposibile mostrare icona PopupMenu",e)
                }
                popupMenu.show()
            }


        }

        fun bind(video: VideoEntity){
            title.text = video.title
            Glide.with(v)
                .load(Uri.parse(video.uri))
                .error(R.drawable.logo_pavone_edges)
                .fitCenter()
                .into(preview)
        }

    }
}