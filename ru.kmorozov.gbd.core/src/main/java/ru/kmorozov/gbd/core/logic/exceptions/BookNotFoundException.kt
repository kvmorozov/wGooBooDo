package ru.kmorozov.gbd.core.logic.exceptions

import java.lang.Exception

class BookNotFoundException: Exception {

    constructor(message:String, cause: Throwable)
}