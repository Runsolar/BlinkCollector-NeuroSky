package com.example.blinkcollector_neurosky.files_list

import android.content.Context
import com.example.blinkcollector_neurosky.R
import com.example.blinkcollector_neurosky.data.FilesListData
import com.example.blinkcollector_neurosky.data.TreeItem
import com.example.blinkcollector_neurosky.data.TreeItem.TreeItemType.*
import com.example.blinkcollector_neurosky.repository.FilesListRepository
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import dagger.Reusable
import javax.inject.Inject

@Reusable
class FilesTreeViewFactory @Inject constructor() {
    @Inject lateinit var filesListRepository: FilesListRepository

    fun createTreeView(context: Context, bases: List<FilesListData>): AndroidTreeView {
        val root = TreeNode.root()
        val filesTreeHolder = FilesTreeHolder(context, filesListRepository)

        bases.map { it.base }.toSet().forEach { baseName ->
            val base = TreeNode(TreeItem(BASE, baseName, baseName))

            val operators = bases.filter { it.base == baseName }
            operators.map { it.operator }.toSet().forEach { operatorName ->
                val operator = TreeNode(TreeItem(OPERATOR, operatorName, "${baseName}/${operatorName}"))

                val blinks = operators.filter { it.operator == operatorName }
                blinks.map { it.blink }.toSet().forEach { blinkName ->
                    val blink = TreeNode(TreeItem(BLINK, blinkName, "${baseName}/${operatorName}/${blinkName}"))

                    val files = blinks.filter { it.blink == blinkName }
                    files.map { it.name }.forEach { fileName ->
                        blink.addChild(TreeNode(TreeItem(FILE, fileName, "${baseName}/${operatorName}/${blinkName}.${fileName}")))
                                .viewHolder = filesTreeHolder;
                    }
                    operator.addChild(blink).viewHolder = filesTreeHolder;
                }
                base.addChild(operator).viewHolder = filesTreeHolder;
            }
            root.addChild(base).viewHolder = filesTreeHolder;
        }

        val treeView = AndroidTreeView(context, root)
        treeView.setDefaultAnimation(true)
        treeView.setUse2dScroll(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom)

        return treeView
    }
}