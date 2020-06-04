package ru.kmorozov.library.data.server.options

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.options.CtxOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage

@Component
@Qualifier("local")
class LocalServerGBDOptions : AbstractServerGBDOptions() {

    @Value("\${library.local.directory}")
    private lateinit var localLibraryPath: String

    override val storage: IStorage
        get() = LocalFSStorage.getStorage(localLibraryPath)

    override fun serverMode(): Boolean {
        return true
    }

    override fun ctxOptions(): CtxOptions {
        return CtxOptions.DEFAULT_CTX_OPTIONS
    }
}
