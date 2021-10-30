package ru.kmorozov.gbd.core.logic.extractors.rfbr

import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor

class RfbrImageExtractor(bookContext: BookContext) : AbstractImageExtractor<RfbrPage>(bookContext, RfbrImageExtractor::class.java)
