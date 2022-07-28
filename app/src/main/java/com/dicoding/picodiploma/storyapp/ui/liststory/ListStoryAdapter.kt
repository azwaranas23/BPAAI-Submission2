package com.dicoding.picodiploma.storyapp.ui.liststory

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.dicoding.picodiploma.storyapp.network.StoryItem
import com.dicoding.picodiploma.storyapp.databinding.ItemRowStoryBinding

class ListStoryAdapter :
    PagingDataAdapter<StoryItem, ListStoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class ListViewHolder(private var binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryItem) {

            binding.pbItemStory.visibility = View.GONE

            binding.imgItemStory.transitionName = story.photoUrl

            Glide.with(binding.imgItemStory.context)
                .load(story.photoUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.cvItem.setOnClickListener {
                            onItemClickCallback.onItemClicked(
                                story,
                                binding
                            )
                        }
                        return false
                    }
                })
                .into(binding.imgItemStory)

            binding.tvItemName.text = story.name
            binding.tvItemDesc.text = story.description
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(story: StoryItem, binding: ItemRowStoryBinding)
    }
}