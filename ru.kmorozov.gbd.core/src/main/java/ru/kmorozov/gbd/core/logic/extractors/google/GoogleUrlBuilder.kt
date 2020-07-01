package ru.kmorozov.gbd.core.logic.extractors.google

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants

class GoogleUrlBuilder(val bookId: String) {

    fun getSigPageUrl(pageId: String, sig: String): String {
        val urlTemplate = GoogleConstants.HTTPS_IMG_TEMPLATE
        val width = if (0 == GBDOptions.imageWidth) GoogleConstants.DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        return urlTemplate.replace(GoogleConstants.BOOK_ID_PLACEHOLDER, bookId) +
                GoogleConstants.IMG_REQUEST_TEMPLATE.replace(GoogleConstants.RQ_PG_PLACEHOLDER, pageId)
                        .replace(GoogleConstants.RQ_SIG_PLACEHOLDER, sig)
                        .replace(GoogleConstants.RQ_WIDTH_PLACEHOLDER, width.toString())
    }

    fun getSinglePageUrl(pageId: String): String {
        val urlTemplate = GoogleConstants.HTTPS_TEMPLATE

        return urlTemplate.replace(GoogleConstants.BOOK_ID_PLACEHOLDER, bookId) +
                GoogleConstants.PAGE_REQUEST_TEMPLATE.replace(GoogleConstants.RQ_PG_PLACEHOLDER, pageId)
    }
}