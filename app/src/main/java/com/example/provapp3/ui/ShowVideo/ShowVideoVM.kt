package com.example.provapp3.ui.ShowVideo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.provapp3.DataClasses.PlayerTrack

class ShowVideoVM : ViewModel() {
    private lateinit var videoURI:String
    private var orientation_landscape = false
    private var dash_media = false
    private var loop_playback = false
    private var map_traks_init = false
    private val resolution = MutableLiveData<String>("Auto")
    private val map_tracks = sortedMapOf<String,PlayerTrack>()

    fun setVideoURI(uri:String){
        videoURI = uri
    }

    fun getVideoURI() = videoURI

    fun getLiveResolution():LiveData<String>{
        return resolution
    }

    fun setCurrentResolution(res:String){
        resolution.value = res
    }

    fun getTrack(resolution:String):PlayerTrack?{
        return map_tracks.get(resolution)
    }

    fun addTrack(track:PlayerTrack){
        map_tracks.put(minOf(track.width,track.height).toString() + "p",track)
    }

    fun getTracksResolution(): Set<String> {
        return map_tracks.keys
    }

    fun getTrackFullResolution():String{
        return if(resolution.value != "Auto"){
                    val track = map_tracks.get(resolution.value)
                    track?.width.toString() + " x " + track?.height
                } else
                    "Unk"
    }

    fun isCurrentResolution(res:String):Boolean{
        return resolution.value == res
    }

    fun setOrientationLandscape(isLandscape:Boolean){
        orientation_landscape = isLandscape
    }
    fun isOrientationLandscape() = orientation_landscape

    fun setDashMedia(){
        dash_media = true
    }
    fun isDashMedia() = dash_media

    fun setMapTracksInit(){
        map_traks_init = true
    }
    fun isMapTraksInit() = map_traks_init

    fun change_loopback_mode():Boolean{
        loop_playback = !loop_playback
        return loop_playback
    }
    fun isLoopPlayback() = loop_playback

}