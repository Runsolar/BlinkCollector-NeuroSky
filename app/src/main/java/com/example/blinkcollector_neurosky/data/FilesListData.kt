package com.example.blinkcollector_neurosky.data

import android.net.Uri
import com.example.blinkcollector_neurosky.repository.DeviceStorage
import java.io.File


data class FilesListData (
        val base: String,
        val operator: String,
        val blink: String,
        var name: String = "",
        val data: List<Point>
) {
    init {
        if (name.isBlank()) {
            val dir = associatedFile();
            var files: Array<File> = arrayOf()
            val baseName = base.replace("database", "db")
            val operatorName = operator.replace("operator", "op")
            val blinkName = "w$blink";
            if (!dir.exists() || dir.listFiles().also { files = it } == null) {
                name = "${baseName}${operatorName}${blinkName}f1.txt".replace(" ", "")
            } else {
                var offset = 1
                var file = File("${dir.path}/${baseName}${operatorName}${blinkName}f${files.size + offset}.txt")
                while (file.exists()) {
                    file = File("${dir.path}/${baseName}${operatorName}${blinkName}f${files.size + ++offset}.txt")
                }
                name = file.name.replace(" ", "")
            }
        }
    }

    fun associatedFile(): File {
        return File(
                Uri.Builder()
                        .appendPath(DeviceStorage.rootDir.path)
                        .appendPath(base)
                        .appendPath(operator)
                        .appendPath(blink)
                        .appendPath(name)
                        .build()
                        .path
        ); }
}