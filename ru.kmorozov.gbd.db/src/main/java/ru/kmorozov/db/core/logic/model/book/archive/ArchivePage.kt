package ru.kmorozov.db.core.logic.model.book.archive

import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.ARCHIVE_IMG_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.ITEM_PATH_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.PID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.SERVER_PLACEHOLDER
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage

class ArchivePage(private val bookId: String, override var order: Int, val itemPath: String, val server: String) : AbstractPage() {

    override val imgUrl: String
        get() = ARCHIVE_IMG_TEMPLATE
                .replace(SERVER_PLACEHOLDER, server)
                .replace(ITEM_PATH_PLACEHOLDER, itemPath)
                .replace(BOOK_ID_PLACEHOLDER, bookId)
                .replace(PID_PLACEHOLDER, pid)

    override val pid: String
        get() = String.format("%04d", order);
}