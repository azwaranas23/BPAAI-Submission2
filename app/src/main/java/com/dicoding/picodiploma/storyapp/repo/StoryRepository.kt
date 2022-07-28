package com.dicoding.picodiploma.storyapp.repo

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.picodiploma.storyapp.database.StoryDatabase
import com.dicoding.picodiploma.storyapp.network.ApiService
import com.dicoding.picodiploma.storyapp.network.StoryItem
import com.dicoding.picodiploma.storyapp.remote.StoryRemoteMediator

class StoryRepository (private val token: String, private val storyDatabase: StoryDatabase, private val apiService: ApiService) {
    fun getStory(): LiveData<PagingData<StoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(token, storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).liveData
    }
}