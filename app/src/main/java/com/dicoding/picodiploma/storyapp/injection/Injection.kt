package com.dicoding.picodiploma.storyapp.injection

import android.content.Context
import com.dicoding.picodiploma.storyapp.database.StoryDatabase
import com.dicoding.picodiploma.storyapp.network.ApiConfig
import com.dicoding.picodiploma.storyapp.repo.StoryRepository

object Injection {
    fun provideRepository(token: String, context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(token, database, apiService)
    }
}