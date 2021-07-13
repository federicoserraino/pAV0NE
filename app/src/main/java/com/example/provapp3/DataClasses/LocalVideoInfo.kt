package com.example.provapp3.DataClasses

data class LocalVideoInfo(
    var codec: String = "Unk",
    var width: Int = 0,
    var height: Int = 0,
    var duration: Long = 0,
    var bitrate: Int = 0,
    var framerate: Int = 0
)