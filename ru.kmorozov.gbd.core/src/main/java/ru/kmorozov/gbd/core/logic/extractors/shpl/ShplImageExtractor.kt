package ru.kmorozov.gbd.core.logic.extractors.shpl

import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplImageExtractor(bookContext: BookContext) : AbstractImageExtractor<ShplPage>(bookContext, ShplImageExtractor::class.java)
