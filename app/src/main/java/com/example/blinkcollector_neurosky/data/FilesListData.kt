package com.example.blinkcollector_neurosky.data


data class FilesListData (
        val name: String,
        val blink: String,
        val operator: String,
        val base: String,
        val personName: String,
        val data: List<Point>
)