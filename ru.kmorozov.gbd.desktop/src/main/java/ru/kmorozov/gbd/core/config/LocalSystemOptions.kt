package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage

/**
 * Created by km on 06.12.2015.
 */
class LocalSystemOptions : IGBDOptions {

    override val bookId: String
        get() = SystemConfigs.lastBookId

    override val storage: IStorage
        get() = LocalFSStorage(SystemConfigs.rootDir)

    override val proxyListFile: String
        get() = SystemConfigs.proxyListFile

    override val imageWidth: Int
        get() = SystemConfigs.resolution

    override fun reloadImages(): Boolean {
        return SystemConfigs.reload
    }

    override fun secureMode(): Boolean {
        return SystemConfigs.secureMode
    }

    override fun pdfOptions(): String {
        return SystemConfigs.pdfMode
    }

    override fun ctxOptions(): CtxOptions {
        return CtxOptions.DEFAULT_CTX_OPTIONS
    }
}