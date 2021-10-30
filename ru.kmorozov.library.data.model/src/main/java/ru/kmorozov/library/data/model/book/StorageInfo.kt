package ru.kmorozov.library.data.model.book

import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by km on 26.12.2016.
 */

@Document
class StorageInfo {

    var filesCount: Long = 0
    var lastChecked: Long = 0

    constructor()

    constructor(filesCount: Long) {
        this.filesCount = filesCount
    }

    fun incFilesCount() {
        this.filesCount++
    }
}
