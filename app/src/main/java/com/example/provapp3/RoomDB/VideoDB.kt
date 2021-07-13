package com.example.provapp3.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoEntity::class], version = 1)
abstract class VideoDB : RoomDatabase() {
    abstract fun videoDao() : VideoDao
    companion object{
        private var instance:VideoDB? = null

        fun getDatabase(context: Context):VideoDB{
            val i = instance
            if(i!=null) return i
            synchronized(this){
                instance = Room.databaseBuilder(context.applicationContext,VideoDB::class.java,"video_database")
                    .build()
                return instance as VideoDB
            }
        }
    }
}