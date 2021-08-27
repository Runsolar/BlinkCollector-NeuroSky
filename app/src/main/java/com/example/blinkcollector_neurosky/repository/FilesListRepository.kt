package com.example.blinkcollector_neurosky.repository

import com.example.blinkcollector_neurosky.data.FilesListData
import com.example.blinkcollector_neurosky.data.Point
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilesListRepository @Inject constructor(
        private val deviceStorage: DeviceStorage
) {
    private val mutableFilesList = MutableStateFlow<List<FilesListData>>(emptyList())
    val filesList = mutableFilesList.asStateFlow()

    init {
        mutableFilesList.value = deviceStorage.loadFiles().toList()
    }

    fun put(name: String, operator: String, directory: String, dataPoints: Array<DataPoint>) {
        val data = FilesListData(
                name = name,
                operator = operator,
                directory = directory,
                data = dataPoints.map { Point(it.x, it.y) }
        )

        mutableFilesList.value += data
        deviceStorage.saveFile(data)
    }

    fun remove(data: FilesListData) {
        mutableFilesList.value -= data
        deviceStorage.removeFile(data)
    }

    fun zip(data: List<FilesListData>) {
        deviceStorage.zipFiles(data);
    }

    fun zipAll() {
        zip(filesList.value);
    }

}