package com.dicoding.picodiploma.storyapp.ui.liststory

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.storyapp.*
import com.dicoding.picodiploma.storyapp.preference.SessionPreferences
import com.dicoding.picodiploma.storyapp.databinding.FragmentListStoryBinding
import com.dicoding.picodiploma.storyapp.databinding.ItemRowStoryBinding
import com.dicoding.picodiploma.storyapp.network.StoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListStoryFragment : Fragment() {

    private lateinit var binding: FragmentListStoryBinding
    private lateinit var viewModel: ListStoryViewModel
    private lateinit var session: SessionPreferences
    private var state: Parcelable? = null
    private var newStoryFlag = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onPause() {
        super.onPause()
        binding.rvStories.layoutManager?.onSaveInstanceState().let {
            state = it
        }
    }

    override fun onResume() {
        super.onResume()
        state?.let {
            binding.rvStories.layoutManager?.onRestoreInstanceState(it)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.app_name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionPreferences(view.context)
        val token = session.getAuthToken() ?: ""

        viewModel = ViewModelProvider(
            this,
            ListStoryViewmodelFactory(token, view.context)
        )[ListStoryViewModel::class.java]

        showRecyclerList()

        ListStoryActivityArgs.fromBundle(arguments as Bundle).toastText?.let {
            showToast(it)
            binding.srlStories.post {
                newStoryFlag = true
                binding.srlStories.isRefreshing = true
            }
            arguments?.clear()
        }

    }


    private fun showRecyclerList() {

        binding.rvStories.layoutManager = LinearLayoutManager(view?.context)

        val adapter = ListStoryAdapter()
        adapter.addLoadStateListener {
            when (it.mediator?.refresh) {
                is LoadState.Loading -> {
                    showLoading(true)
                    showError(false)
                }
                is LoadState.Error -> {
                    showLoading(false)
                    if (adapter.itemCount < 1) {
                        (it.mediator?.refresh as LoadState.Error).error.message?.also { errorMsg ->
                            showError(true, errorMsg)
                        }
                        binding.btnRetry.setOnClickListener { adapter.refresh() }
                    } else showError(false)
                }
                is LoadState.NotLoading -> {
                    showLoading(false)
                    if (adapter.itemCount < 1) {
                        showError(true, getString(R.string.no_data_to_display))
                        binding.btnRetry.setOnClickListener { adapter.refresh() }
                    } else showError(false)
                }
                else -> {}
            }
            binding.srlStories.setOnRefreshListener { adapter.refresh() }
        }
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        viewModel.stories.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
        adapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun onItemClicked(story: StoryItem, binding: ItemRowStoryBinding) {
                binding.pbItemStory.visibility = View.VISIBLE
                showStory(story, binding)
            }
        })
    }

    private fun showStory(story: StoryItem, binding: ItemRowStoryBinding) {
        val extras = FragmentNavigatorExtras(
            binding.imgItemStory to story.photoUrl
        )
        val toDetailStoryFragment =
            ListStoryFragmentDirections.actionListStoryFragmentToDetailStoryFragment(
                story
            )
        view?.findNavController()?.navigate(toDetailStoryFragment, extras)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.srlStories.isRefreshing = true
            binding.rvStories.visibility = View.GONE
        } else {
            binding.srlStories.isRefreshing = false
            if (newStoryFlag) {
                newStoryFlag = false
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        binding.rvStories.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(view?.context, text, Toast.LENGTH_SHORT).show()
    }

    private fun showError(isError: Boolean, msg: String = "") {
        if (isError) {
            binding.tvErrorMsg.text = msg
            binding.rvStories.visibility = View.GONE
            binding.tvErrorMsg.visibility = View.VISIBLE
            binding.btnRetry.visibility = View.VISIBLE
        } else {
            binding.rvStories.visibility = View.VISIBLE
            binding.tvErrorMsg.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
        }
    }
}