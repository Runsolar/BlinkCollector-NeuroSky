package com.example.blinkcollector_neurosky.files_list

import androidx.recyclerview.widget.DiffUtil

class FilesListDiffUtil : DiffUtil.ItemCallback<FilesListItem>() {
    override fun areItemsTheSame(oldItem: FilesListItem, newItem: FilesListItem): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: FilesListItem, newItem: FilesListItem): Boolean {
        return oldItem == newItem
    }
}
