package ru.kmorozov.library.data.server.state

import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.providers.IProxyListProvider

@Component
class ServerState {

    public val proxyState: IProxyListProvider
        get() = AbstractProxyListProvider.INSTANCE
}