package com.example.blinkcollector_neurosky.files_list

import androidx.recyclerview.widget.DiffUtil
import com.example.blinkcollector_neurosky.data.FilesListData
import javax.inject.Inject

class FilesListDiffUtil @Inject constructor() : DiffUtil.ItemCallback<FilesListData>() {
    override fun areItemsTheSame(oldData: FilesListData, newData: FilesListData): Boolean {
        return oldData == newData
    }

    override fun areContentsTheSame(oldData: FilesListData, newData: FilesListData): Boolean {
        return oldData == newData
    }
}
