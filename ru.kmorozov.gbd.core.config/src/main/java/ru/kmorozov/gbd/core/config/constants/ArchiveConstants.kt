package ru.kmorozov.gbd.core.config.constants

object ArchiveConstants {
    const val BASE_URL = "https://archive.org/details/%BOOK_ID%"
    const val ARCHIVE_IMG_TEMPLATE = "https://%SERVER%/BookReader/BookReaderImages.php?zip=/3/items/%BOOK_ID%/%BOOK_ID%_jp2.zip&file=%BOOK_ID%_jp2/%BOOK_ID%_%PID%.jp2&scale=4&rotate=0"

    const val BOOK_ID_PLACEHOLDER = "%BOOK_ID%"
    const val SERVER_PLACEHOLDER = "%SERVER%"
    const val PID_PLACEHOLDER = "%PID%"
}