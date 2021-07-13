package com.example.provapp3.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_table")
data class VideoEntity(
    @PrimaryKey val uri: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "size") var size: Long
)