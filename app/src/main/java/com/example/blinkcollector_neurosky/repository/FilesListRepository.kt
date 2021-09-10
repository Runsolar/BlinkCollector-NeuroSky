package com.example.blinkcollector_neurosky.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.example.blinkcollector_neurosky.BuildConfig
import com.example.blinkcollector_neurosky.data.FilesListData
import com.example.blinkcollector_neurosky.data.Point
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
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

    fun put(blink: String, operator: String, base: String, dataPoints: Array<DataPoint>) {
        val data = FilesListData(
                blink = blink,
                operator = operator,
                base = base,
                data = dataPoints.filterNotNull().map { Point(it.x, it.y) }
        )

        mutableFilesList.value += data
        deviceStorage.saveFile(data)
    }

    fun remove(path: String) {
        deviceStorage.deleteDirectory(path)
        mutableFilesList.value = deviceStorage.loadFiles().toList()
    }

    fun remove(data: FilesListData) {
        mutableFilesList.value -= data
        deviceStorage.removeFile(data)
    }

    fun prepareForShare(path: String): File {
        return deviceStorage.prepareForShare(path);
    }

    fun zip(path: String): File {
        return deviceStorage.zipFiles(path);
    }

    fun zip(data: List<FilesListData>): File {
        return deviceStorage.zipFiles(data);
    }

    fun zipAll(): File {
        return zip(filesList.value);
    }

    fun normalize(path: String) {
        deviceStorage.normalizeBase(path);
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (file.path.endsWith(".txt")) {
            intent.setType("text/plain")
        } else {
            intent.setType("application/zip")
        }
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(context, intent, Bundle.EMPTY)
    }

}