package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
interface IImageExtractor : Runnable {

    fun newProxyEvent(proxy: HttpHostExt)
}
