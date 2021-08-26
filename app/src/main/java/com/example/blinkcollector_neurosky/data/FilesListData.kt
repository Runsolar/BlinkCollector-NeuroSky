package com.example.blinkcollector_neurosky.data


data class FilesListData (
    val name: String,
    val directory: String,
    val operator: String,
    val data: List<Point>
)