package com.example.blinkcollector_neurosky.files_list

import android.content.Context
import com.example.blinkcollector_neurosky.data.FilesListData
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import javax.inject.Inject

class TreeViewController @Inject constructor(
        private val filesTreeViewFactory: FilesTreeViewFactory
) {
    private val root = TreeNode.root()

    fun createTreeView(
            context: Context,
            files: List<FilesListData>,
            filesTreeListener: FilesTreeListener
    ): AndroidTreeView {
        return filesTreeViewFactory.createTreeView(context, root, files, filesTreeListener)
    }

    fun unselectAll() {
        root.children.forEach { database ->
            database.children.forEach { operator ->
               operator.children.forEach { blink ->
                   blink.children.forEach { file ->
                       (file.viewHolder as FilesTreeHolder).setSelected(false)
                   }
               }
            }
        }
    }
}