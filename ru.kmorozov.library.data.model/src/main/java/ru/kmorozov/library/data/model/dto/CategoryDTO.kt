package ru.kmorozov.library.data.model.dto

import ru.kmorozov.library.data.model.book.Category

/**
 * Created by sbt-morozov-kv on 04.04.2017.
 */
class CategoryDTO {

    lateinit var id: String
    lateinit var name: String

    constructor() {}

    constructor(category: Category) {
        this.id = category.id
        this.name = category.name
    }
}
