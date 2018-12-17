package ru.kmorozov.gbd.core.config.options

class CtxOptions {

    val ctxMode: CtxMode
    val connectionParams: String

    enum class CtxMode {
        FILE,
        MONGO
    }

    constructor(ctxMode: String, connectionParams: String) {
        this.ctxMode = CtxMode.valueOf(ctxMode)
        this.connectionParams = connectionParams
    }

    internal constructor(ctxMode: CtxMode, connectionParams: String) {
        this.ctxMode = ctxMode
        this.connectionParams = connectionParams
    }

    companion object {

        val DEFAULT_CTX_OPTIONS = CtxOptions(CtxMode.FILE, "books.ctx")
    }
}
