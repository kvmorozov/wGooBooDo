package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.config.options.ScanOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage

/**
 * Created by km on 06.12.2015.
 */
class LocalSystemOptions : IGBDOptions {

    override val bookId: String
        get() = SystemConfigs.lastBookId

    override val storage: IStorage
        get() = LocalFSStorage.getStorage(SystemConfigs.rootDir)

    override val proxyListFile: String
        get() = SystemConfigs.proxyListFile

    override val imageWidth: Int
        get() = SystemConfigs.resolution

    override val reloadImages: Boolean
        get() = SystemConfigs.reloadImages

    override fun scanOptions(): ScanOptions {
        TODO("Not yet implemented")
    }

    override val secureMode: Boolean
        get() = SystemConfigs.secureMode


    override fun pdfOptions(): String {
        return SystemConfigs.pdfMode
    }

    override fun ctxOptions(): CtxOptions {
        return CtxOptions.DEFAULT_CTX_OPTIONS
    }
}
