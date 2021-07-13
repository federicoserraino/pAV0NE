package com.example.provapp3.RoomDB

import android.app.Application
import androidx.lifecycle.LiveData


class VideoRepository(application: Application) {
    private val videoDao:VideoDao

  init {
      val db = VideoDB.getDatabase(application)
      videoDao = db.videoDao()
  }

    fun getAllVideos():LiveData<List<VideoEntity>>{
        return videoDao.getAllVideos()
    }

    suspend fun insertVideo(videoEntity: VideoEntity){
        videoDao.insert(videoEntity)
    }

    suspend fun updateVideo(videoEntity: VideoEntity){
        videoDao.update(videoEntity)
    }

    suspend fun removeVideo(videoEntity: VideoEntity){
        videoDao.delete(videoEntity)
    }

    fun getVideo(uri:String):LiveData<VideoEntity>{
        return videoDao.getVideo(uri)
    }


}