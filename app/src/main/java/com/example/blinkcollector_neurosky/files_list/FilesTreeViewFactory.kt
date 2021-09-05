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
class FilesTreeViewFactory @Inject constructor(
        private val filesListRepository: FilesListRepository
) {
    fun createTreeView(
            context: Context,
            bases: List<FilesListData>,
            filesTreeListener: FilesTreeListener
    ): AndroidTreeView {
        val root = TreeNode.root()

        bases.map { it.base }.toSet().forEach { baseName ->
            val base = TreeNode(TreeItem(BASE, baseName, baseName))
            base.viewHolder = FilesTreeHolder(context, filesListRepository, filesTreeListener)

            val operators = bases.filter { it.base == baseName }
            operators.map { it.operator }.toSet().forEach { operatorName ->
                val operator = TreeNode(TreeItem(OPERATOR, operatorName, "${baseName}/${operatorName}"))
                operator.viewHolder = FilesTreeHolder(context, filesListRepository, filesTreeListener)

                val blinks = operators.filter { it.operator == operatorName }
                blinks.map { it.blink }.toSet().forEach { blinkName ->
                    val blink = TreeNode(TreeItem(BLINK, blinkName, "${baseName}/${operatorName}/${blinkName}"))
                    blink.viewHolder = FilesTreeHolder(context, filesListRepository, filesTreeListener)

                    val files = blinks.filter { it.blink == blinkName }
                    files.map { it.name }.forEach { fileName ->
                        val filePath = "$baseName/$operatorName/$blinkName.$fileName"
                        val file = TreeNode(TreeItem(FILE, fileName, filePath))
                        file.viewHolder = FilesTreeHolder(context, filesListRepository, filesTreeListener)
                        blink.addChild(file)
                    }
                    operator.addChild(blink)
                }
                base.addChild(operator)
            }
            root.addChild(base)
        }

        val treeView = AndroidTreeView(context, root)
        treeView.setDefaultAnimation(true)
        treeView.setUse2dScroll(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom)

        return treeView
    }
}