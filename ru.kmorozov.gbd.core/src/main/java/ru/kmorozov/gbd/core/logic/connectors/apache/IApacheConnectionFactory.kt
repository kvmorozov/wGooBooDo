package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.http.client.HttpClient
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt

interface IApacheConnectionFactory {

    fun getClient(proxy: HttpHostExt = HttpHostExt.NO_PROXY, withTimeout: Boolean = false): HttpClient

    fun closeAllConnections()
}