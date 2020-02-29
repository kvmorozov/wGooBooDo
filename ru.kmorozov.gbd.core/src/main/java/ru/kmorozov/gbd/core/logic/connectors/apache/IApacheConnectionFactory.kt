package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.classic.HttpClient
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt

interface IApacheConnectionFactory {

    fun getClient(proxy: HttpHostExt = HttpHostExt.NO_PROXY, withTimeout: Boolean = false): HttpClient

    fun closeAllConnections()
}