package com.example.provapp3.ui.LocalVideos

import android.net.Uri
import androidx.lifecycle.*
import com.example.provapp3.RoomDB.VideoEntity
import com.example.provapp3.RoomDB.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LocalVideosVM(private val repository: VideoRepository) : ViewModel() {


    fun getAllVideos() : LiveData<List<VideoEntity>>{
        return repository.getAllVideos()
    }

    fun addVideo(video: VideoEntity){
        viewModelScope.launch(Dispatchers.IO) {
                repository.insertVideo(video)
        }
    }

    fun updateVideo(video: VideoEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVideo(video)
        }
    }

    fun removeVideo(video:VideoEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeVideo(video)
        }
    }

    class LocalVideoViewModelFactory(private val repository: VideoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocalVideosVM::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LocalVideosVM(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}