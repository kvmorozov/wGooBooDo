package ru.kmorozov.library.data.loader.processors

interface IGbdProcessor : IProcessor {

    fun load(bookId: String)
}
