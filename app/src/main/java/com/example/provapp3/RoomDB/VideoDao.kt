package com.example.provapp3.RoomDB

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     suspend fun insert(video:VideoEntity)

     @Query("SELECT * FROM video_table")
     fun getAllVideos():LiveData<List<VideoEntity>>

    @Update
    suspend fun update(video:VideoEntity)

    @Delete
    suspend fun delete(video:VideoEntity)

    @Query("SELECT * FROM video_table WHERE uri = :uri")
    fun getVideo(uri:String):LiveData<VideoEntity>

}