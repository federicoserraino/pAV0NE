package com.example.provapp3.ui.OnlineVideos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashVideosVM : ViewModel(){
    private val media_uri = MutableLiveData<String>().apply { value = "" }

    fun getMediaURI() : LiveData<String>{
        return media_uri
    }

    fun setMediaURI(media_uri: String){
        this.media_uri.value = media_uri
    }

}