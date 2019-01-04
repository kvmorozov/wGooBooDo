package ru.kmorozov.gbd.core.config

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.options.AuthOptions
import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.config.options.PdfOptions

/**
 * Created by km on 01.12.2015.
 */
object GBDOptions {

    private lateinit var INSTANCE: IGBDOptions

    val bookId: String
        get() = INSTANCE.bookId

    val storage: IStorage
        get() = INSTANCE.storage

    val proxyListFile: String?
        get() = INSTANCE.proxyListFile

    val imageWidth: Int
        get() = INSTANCE.imageWidth

    val isValidConfig: Boolean
        get() = INSTANCE.storage.isValidOrCreate

    val ctxOptions: CtxOptions
        get() = INSTANCE.ctxOptions()

    fun init(optionHolder: IGBDOptions) {
        INSTANCE = optionHolder
    }

    val reloadImages: Boolean
        get() = INSTANCE.reloadImages()

    val secureMode: Boolean
        get() = INSTANCE.secureMode()

    val pdfOptions: PdfOptions
        get() = if (StringUtils.isEmpty(INSTANCE.pdfOptions())) PdfOptions.DEFAULT_MODE else PdfOptions.getOption(INSTANCE.pdfOptions())

    fun getImageWidth(defaultValue: Int): Int {
        return if (0 == INSTANCE.imageWidth) defaultValue else INSTANCE.imageWidth
    }

    val authOptions: AuthOptions?
        get() = INSTANCE.authOptions()
}
