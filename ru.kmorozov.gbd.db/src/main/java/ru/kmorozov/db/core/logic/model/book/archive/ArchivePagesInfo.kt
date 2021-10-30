package ru.kmorozov.db.core.logic.model.book.archive

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPagesInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

class ArchivePagesInfo(override val pages: Array<IPage>) : AbstractPagesInfo()