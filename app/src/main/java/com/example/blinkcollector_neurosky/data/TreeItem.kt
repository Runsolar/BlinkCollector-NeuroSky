package com.example.blinkcollector_neurosky.data

data class TreeItem (val type: TreeItemType, val name: String) {
    enum class TreeItemType {
        FILE,
        BLINK,
        OPERATOR,
        BASE,
        APP_FOLDER
    }
}