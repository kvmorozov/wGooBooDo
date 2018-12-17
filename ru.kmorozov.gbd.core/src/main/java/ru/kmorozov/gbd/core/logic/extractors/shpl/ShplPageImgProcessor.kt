package ru.kmorozov.gbd.core.logic.extractors.shpl

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
class ShplPageImgProcessor(bookContext: BookContext, page: ShplPage, usedProxy: HttpHostExt) : SimplePageImgProcessor<ShplPage>(bookContext, page, usedProxy)
