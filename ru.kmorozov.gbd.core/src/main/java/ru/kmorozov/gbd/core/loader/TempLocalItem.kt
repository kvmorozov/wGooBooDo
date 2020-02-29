package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.File
import java.io.IOException

class TempLocalItem @Throws(IOException::class)
constructor(storage: LocalFSStorage, page: IPage, imgFormat: String) : MayBePageItem(File.createTempFile(page.order.toString() + '_'.toString() + page.pid, imgFormat), page)
