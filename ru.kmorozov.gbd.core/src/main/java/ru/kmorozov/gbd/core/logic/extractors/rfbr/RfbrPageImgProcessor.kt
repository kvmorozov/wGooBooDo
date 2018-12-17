package ru.kmorozov.gbd.core.logic.extractors.rfbr

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
class RfbrPageImgProcessor(bookContext: BookContext, page: RfbrPage, usedProxy: HttpHostExt) : SimplePageImgProcessor<RfbrPage>(bookContext, page, usedProxy)
