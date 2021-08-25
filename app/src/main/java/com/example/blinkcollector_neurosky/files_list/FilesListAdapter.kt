package com.example.blinkcollector_neurosky.files_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkcollector_neurosky.databinding.FilesListItemBinding
import javax.inject.Inject


class FilesListAdapter @Inject constructor(
        filesListDiffUtil: FilesListDiffUtil
) : ListAdapter<FilesListItem, FilesListAdapter.ViewHolder>(filesListDiffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FilesListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(
            private val binding: FilesListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilesListItem) {
            binding.name.text = item.name

            binding.share.setOnClickListener {
                //todo share click
            }

            binding.remove.setOnClickListener {
                //todo remove click
            }
        }
    }
}