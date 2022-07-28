package com.dicoding.picodiploma.storyapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.picodiploma.storyapp.remote.RemoteKeys
import com.dicoding.picodiploma.storyapp.remote.RemoteKeysDao
import com.dicoding.picodiploma.storyapp.network.StoryItem

@Database(
    entities = [StoryItem::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)

abstract class StoryDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "story_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}