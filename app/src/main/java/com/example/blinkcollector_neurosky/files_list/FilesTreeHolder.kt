package com.example.blinkcollector_neurosky.files_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.blinkcollector_neurosky.R
import com.example.blinkcollector_neurosky.databinding.FilesTreeItemBinding
import com.example.blinkcollector_neurosky.data.TreeItem
import com.example.blinkcollector_neurosky.data.TreeItem.TreeItemType.*
import com.unnamed.b.atv.model.TreeNode

class FilesTreeHolder(context: Context) : TreeNode.BaseNodeViewHolder<TreeItem>(context) {
    override fun createNodeView(node: TreeNode, item: TreeItem): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.files_tree_item, null, false)
        val binding = FilesTreeItemBinding.bind(view)

        binding.treeName.text = item.name

        binding.treeIcon.setImageResource(when (item.type) {
            FILE -> R.drawable.ic_file
            BLINK, OPERATOR -> R.drawable.ic_folder
            BASE -> R.drawable.ic_base
            APP_FOLDER -> R.drawable.ic_hub
        }
        )

        return view
    }
}