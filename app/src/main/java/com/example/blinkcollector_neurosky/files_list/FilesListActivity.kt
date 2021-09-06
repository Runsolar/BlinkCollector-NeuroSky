package com.example.blinkcollector_neurosky.files_list


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blinkcollector_neurosky.databinding.ActivityFilesListBinding
import com.example.blinkcollector_neurosky.repository.FilesListRepository
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FilesListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilesListBinding

    @Inject
    lateinit var filesListRepository: FilesListRepository

    @Inject
    lateinit var filesTreeViewFactory: FilesTreeViewFactory

    private lateinit var innerScope: CoroutineScope
    private lateinit var treeView: AndroidTreeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        innerScope = MainScope()
        innerScope.launch {
            filesListRepository.filesList.take(1).collect { files ->
                treeView = filesTreeViewFactory.createTreeView(
                        this@FilesListActivity,
                        files,
                        object : FilesTreeListener {
                            override fun removeNode(node: TreeNode) {
                                treeView.removeNode(node)
                            }
                        }
                )
                binding.treeViewContainer.removeAllViews()
                binding.treeViewContainer.addView(treeView.view)
                treeView.expandLevel(2)
            }
        }
    }

    override fun onDetachedFromWindow() {
        innerScope.cancel()
        super.onDetachedFromWindow()
    }
}