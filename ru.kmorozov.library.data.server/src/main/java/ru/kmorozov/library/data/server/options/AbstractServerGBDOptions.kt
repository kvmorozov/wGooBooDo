package ru.kmorozov.library.data.server.options

import ru.kmorozov.gbd.core.config.IGBDOptions
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.options.CtxOptions

abstract class AbstractServerGBDOptions : IGBDOptions {
    override var bookId: String = ""
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val storage: IStorage
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val proxyListFile: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val imageWidth: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val reloadImages: Boolean
        get() = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override val secureMode: Boolean
        get() = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override fun pdfOptions(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ctxOptions(): CtxOptions {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
