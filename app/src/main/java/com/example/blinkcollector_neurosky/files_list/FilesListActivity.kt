package com.example.blinkcollector_neurosky.files_list


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.blinkcollector_neurosky.databinding.ActivityFilesListBinding
import com.example.blinkcollector_neurosky.repository.FilesListRepository
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.Series
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
    lateinit var treeViewController: TreeViewController

    private lateinit var innerScope: CoroutineScope
    private lateinit var treeView: AndroidTreeView

    private val filesTreeListener =  object : FilesTreeListener {
        override fun removeNode(node: TreeNode) {
            treeView.removeNode(node)
        }

        override fun showChart(path: String) {
            val filesListData = filesListRepository.getFilesListData(path) ?: return
            val dataPoints = filesListData.data.map { DataPoint(it.x, it.y) }.toTypedArray()
            showPreviewGraph(LineGraphSeries(dataPoints))
        }

        override fun hideChart() {
            hidePreviewGraph()
        }

        override fun unselectAll() {
            treeViewController.unselectAll()
        }
    }

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
                treeView = treeViewController.createTreeView(
                        this@FilesListActivity,
                        files,
                       filesTreeListener
                )
                binding.treeViewContainer.removeAllViews()
                binding.treeViewContainer.addView(treeView.view)
                treeView.expandLevel(2)
            }
        }

        binding.previewContainer.viewport.isScrollable = true
        binding.previewContainer.viewport.isXAxisBoundsManual = true
        binding.previewContainer.viewport.setMinX(0.0)
        binding.previewContainer.viewport.setMaxX(800.0)

        binding.btnClose.setOnClickListener {
            hidePreviewGraph()
            treeViewController.unselectAll()
        }
    }

    override fun onDetachedFromWindow() {
        innerScope.cancel()
        super.onDetachedFromWindow()
    }

    private fun showPreviewGraph(series: Series<DataPoint>) {
        binding.previewContainer.visibility = View.VISIBLE
        binding.btnClose.visibility = View.VISIBLE
        binding.previewContainer.removeAllSeries()
        binding.previewContainer.addSeries(series)
    }

    private fun hidePreviewGraph() {
        binding.previewContainer.visibility = View.GONE
        binding.btnClose.visibility = View.GONE
        binding.previewContainer.removeAllSeries()
    }
}