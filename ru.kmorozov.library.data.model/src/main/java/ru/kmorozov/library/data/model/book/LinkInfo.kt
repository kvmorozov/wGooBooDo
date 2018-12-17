package ru.kmorozov.library.data.model.book

import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by sbt-morozov-kv on 11.04.2017.
 */
class LinkInfo {

    var isBroken: Boolean = false

    @DBRef
    var linkedStorage: Storage? = null

    @DBRef
    var linkedBook: Book? = null
}
