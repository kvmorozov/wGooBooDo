package ru.kmorozov.gbd.core.config.constants

object ArchiveConstants {
    const val BASE_URL = "https://archive.org/details/%BOOK_ID%"
    const val ARCHIVE_IMG_TEMPLATE = "https://%SERVER%/BookReader/BookReaderImages.php?zip=%ITEM_PATH%/%BOOK_ID%_jp2.zip&file=%BOOK_ID%_jp2/%BOOK_ID%_%PID%.jp2&id=%BOOK_ID%_%PID%&scale=4&rotate=0"

    const val BOOK_ID_PLACEHOLDER = "%BOOK_ID%"
    const val ITEM_PATH_PLACEHOLDER = "%ITEM_PATH%"
    const val SERVER_PLACEHOLDER = "%SERVER%"
    const val PID_PLACEHOLDER = "%PID%"
}