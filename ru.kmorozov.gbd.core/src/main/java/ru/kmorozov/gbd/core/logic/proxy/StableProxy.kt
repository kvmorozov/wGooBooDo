package ru.kmorozov.gbd.core.logic.proxy

import java.net.InetSocketAddress
import java.net.Proxy

class StableProxy : HttpHostExt {

    constructor() : super()

    constructor(host: InetSocketAddress, proxy: Proxy) : super(host, proxy)

    override fun registerFailure() {

    }
}