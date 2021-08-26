package com.example.blinkcollector_neurosky.files_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkcollector_neurosky.data.FilesListData
import com.example.blinkcollector_neurosky.databinding.FilesListItemBinding
import com.example.blinkcollector_neurosky.repository.FilesListRepository
import javax.inject.Inject


class FilesListAdapter @Inject constructor(
    filesListDiffUtil: FilesListDiffUtil,
    private val filesListRepository: FilesListRepository
) : ListAdapter<FilesListData, FilesListAdapter.ViewHolder>(filesListDiffUtil) {
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
        fun bind(data: FilesListData) {
            binding.name.text = data.name

            binding.share.setOnClickListener {
                //todo share click
            }

            binding.remove.setOnClickListener {
                filesListRepository.remove(data)
            }
        }
    }
}