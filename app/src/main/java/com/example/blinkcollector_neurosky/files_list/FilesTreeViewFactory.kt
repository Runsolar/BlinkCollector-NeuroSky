package com.example.blinkcollector_neurosky.files_list

import android.content.Context
import com.example.blinkcollector_neurosky.R
import com.example.blinkcollector_neurosky.data.FilesListData
import com.example.blinkcollector_neurosky.data.TreeItem
import com.example.blinkcollector_neurosky.data.TreeItem.TreeItemType.*
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import dagger.Reusable
import javax.inject.Inject

@Reusable
class FilesTreeViewFactory @Inject constructor() {
    fun createTreeView(context: Context, bases: List<FilesListData>): AndroidTreeView {
        val root = TreeNode.root()

        bases.map { it.base }.toSet().forEach { baseName ->
            val base = TreeNode(TreeItem(BASE, baseName))

            val operators = bases.filter { it.base == baseName }
            operators.map { it.operator }.toSet().forEach { operatorName ->
                val operator = TreeNode(TreeItem(OPERATOR, operatorName))

                val blinks = operators.filter { it.operator == operatorName }
                blinks.map { it.blink }.toSet().forEach { blinkName ->
                    val blink = TreeNode(TreeItem(BLINK, blinkName))

                    val files = blinks.filter { it.blink == blinkName }
                    files.map { it.name }.forEach { fileName ->
                        blink.addChild(TreeNode(TreeItem(FILE, fileName)))
                    }
                    operator.addChild(blink)
                }
                base.addChild(operator)
            }
            root.addChild(base)
        }

        val treeView = AndroidTreeView(context, root)
        treeView.setDefaultViewHolder(FilesTreeHolder(context).javaClass)
        treeView.setDefaultAnimation(true)
        treeView.setUse2dScroll(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom)

        return treeView
    }
}