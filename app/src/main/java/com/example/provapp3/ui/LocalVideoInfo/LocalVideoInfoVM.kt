package com.example.provapp3.ui.LocalVideoInfo

import androidx.lifecycle.*
import com.example.provapp3.DataClasses.LocalVideoInfo
import com.example.provapp3.RoomDB.VideoEntity
import com.example.provapp3.RoomDB.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalVideoInfoVM(private val repository: VideoRepository) : ViewModel(){

    private lateinit var videoEntity:VideoEntity
    private val video_info:MutableLiveData<LocalVideoInfo> by lazy { MutableLiveData<LocalVideoInfo>() }

    fun setVideo(uri:String,title:String,size:Long){
        videoEntity = VideoEntity(uri,title,size)
    }

    fun getVideo():LiveData<VideoEntity>{
        return repository.getVideo(videoEntity.uri)
    }

    fun getVideoURI():String{
        return videoEntity.uri
    }

    fun setVideoInfo(codec: String, width: Int, height: Int, duration: Long, bitrate: Int, framerate: Int) {
            video_info.value = LocalVideoInfo(codec,width,height,duration,bitrate,framerate)
    }

    fun getVideoInfo():LiveData<LocalVideoInfo>{
        return video_info
    }

    fun updateVideoTitle(title:String){
        videoEntity.title = title
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVideo(videoEntity)
        }
    }

    class InfoVideoViewModelFactory(private val repository: VideoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocalVideoInfoVM::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LocalVideoInfoVM(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}