package ru.kmorozov.gbd.core.config.constants

object GoogleConstants {

    const val DEFAULT_PAGE_WIDTH = 1280
    const val HTTP_TEMPLATE = "http://books.google.ru/books?id=%BOOK_ID%"
    const val HTTPS_TEMPLATE = "https://books.google.ru/books?id=%BOOK_ID%"
    const val HTTPS_IMG_TEMPLATE = "https://books.google.ru/books/content?id=%BOOK_ID%"

    const val BOOK_ID_PLACEHOLDER = "%BOOK_ID%"
    const val RQ_PG_PLACEHOLDER = "%PG%"
    const val RQ_SIG_PLACEHOLDER = "%SIG%"
    const val RQ_WIDTH_PLACEHOLDER = "%WIDTH%"

    const val PAGES_REQUEST_TEMPLATE = "&lpg=PP1&hl=en&pg=%PG%&jscmd=click3"
    const val IMG_REQUEST_TEMPLATE = "&pg=%PG%&img=1&zoom=3&hl=ru&sig=%SIG%&w=%WIDTH%"
}
