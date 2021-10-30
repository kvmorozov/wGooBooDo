package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt

class BookTask(val book: BookContext, val page: IPage, val proxy: HttpHostExt)