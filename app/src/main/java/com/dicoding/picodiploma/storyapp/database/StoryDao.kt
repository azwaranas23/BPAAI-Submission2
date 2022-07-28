package com.dicoding.picodiploma.storyapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.picodiploma.storyapp.network.StoryItem

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(story: List<StoryItem>)

    @Query("SELECT * FROM story")
    fun getAllStories(): PagingSource<Int, StoryItem>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}