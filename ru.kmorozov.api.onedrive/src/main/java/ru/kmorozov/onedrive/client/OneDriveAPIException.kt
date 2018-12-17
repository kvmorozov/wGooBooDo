package ru.kmorozov.onedrive.client

import java.io.IOException

class OneDriveAPIException : IOException {

    val code: Int

    constructor(code: Int, message: String) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String, cause: Throwable) : super(message, cause) {
        this.code = code
    }
}
