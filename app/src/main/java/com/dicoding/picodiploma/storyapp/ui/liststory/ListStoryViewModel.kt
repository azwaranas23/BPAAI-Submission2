package com.dicoding.picodiploma.storyapp.ui.liststory

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.storyapp.injection.Injection
import com.dicoding.picodiploma.storyapp.network.StoryItem
import com.dicoding.picodiploma.storyapp.repo.StoryRepository

class ListStoryViewmodelFactory(private val token: String, private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListStoryViewModel(Injection.provideRepository(token, context)) as T
    }
}

class ListStoryViewModel(storyRepository: StoryRepository) : ViewModel() {

    val stories: LiveData<PagingData<StoryItem>> =
        storyRepository.getStory().cachedIn(viewModelScope)
}