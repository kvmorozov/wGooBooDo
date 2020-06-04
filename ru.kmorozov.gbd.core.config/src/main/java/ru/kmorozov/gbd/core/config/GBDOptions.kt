package ru.kmorozov.gbd.core.config

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.options.AuthOptions
import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.config.options.PdfOptions

/**
 * Created by km on 01.12.2015.
 */
object GBDOptions {

    private lateinit var _INSTANCE: IGBDOptions

    val bookId: String
        get() = _INSTANCE.bookId

    val storage: IStorage
        get() = _INSTANCE.storage

    val proxyListFile: String
        get() = _INSTANCE.proxyListFile

    val imageWidth: Int
        get() = _INSTANCE.imageWidth

    val isValidConfig: Boolean
        get() = _INSTANCE.storage.isValidOrCreate

    val ctxOptions: CtxOptions
        get() = _INSTANCE.ctxOptions()

    fun init(optionHolder: IGBDOptions) {
        _INSTANCE = optionHolder
    }

    val reloadImages: Boolean
        get() = _INSTANCE.reloadImages

    val secureMode: Boolean
        get() = _INSTANCE.secureMode

    val debugEnabled: Boolean
        get() = _INSTANCE.debugEnabled

    val scanEnabled: Boolean
        get() = _INSTANCE.scanEnabled

    val pdfOptions: PdfOptions
        get() = if (StringUtils.isEmpty(_INSTANCE.pdfOptions())) PdfOptions.DEFAULT_MODE else PdfOptions.getOption(_INSTANCE.pdfOptions())

    fun getImageWidth(defaultValue: Int): Int {
        return if (0 == _INSTANCE.imageWidth) defaultValue else _INSTANCE.imageWidth
    }

    val authOptions: AuthOptions?
        get() = _INSTANCE.authOptions()

    val serverMode: Boolean
        get() = _INSTANCE.serverMode()
}
