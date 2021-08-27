package com.example.blinkcollector_neurosky.files_list


import android.R.layout.simple_spinner_dropdown_item
import android.R.layout.simple_spinner_item
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blinkcollector_neurosky.databinding.ActivityFilesListBinding
import com.example.blinkcollector_neurosky.repository.FilesListRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FilesListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilesListBinding

    @Inject
    lateinit var filesListAdapter: FilesListAdapter

    @Inject
    lateinit var filesListRepository: FilesListRepository

    private lateinit var innerScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.adapter = filesListAdapter
        binding.list.layoutManager = LinearLayoutManager(this)

        val directories = arrayOf("blink2", "blink3", "blink4")
        val directoryAdapter = ArrayAdapter(this, simple_spinner_item, directories)
        directoryAdapter.setDropDownViewResource(simple_spinner_dropdown_item)
        binding.directory.adapter = directoryAdapter

        val operators = arrayOf("operator2", "operator3", "operator4")
        val operatorAdapter = ArrayAdapter(this, simple_spinner_item, operators)
        directoryAdapter.setDropDownViewResource(simple_spinner_dropdown_item)
        binding.operator.adapter = operatorAdapter

        binding.floatingActionButton.setOnClickListener {
            filesListRepository.zipAll();
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        innerScope = MainScope()
        innerScope.launch {
            filesListRepository.filesList.collect {
                filesListAdapter.submitList(it)
            }
        }
    }

    override fun onDetachedFromWindow() {
        innerScope.cancel()
        super.onDetachedFromWindow()
    }
}