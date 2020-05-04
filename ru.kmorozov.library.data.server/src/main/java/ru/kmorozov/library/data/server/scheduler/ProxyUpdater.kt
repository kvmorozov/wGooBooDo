package ru.kmorozov.library.data.server.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.logic.proxy.AbstractProxyListProvider.Companion.INSTANCE
import ru.kmorozov.gbd.core.logic.proxy.UrlType

@Component
class ProxyUpdater {

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun proxyUpdater() {
        INSTANCE.reset()
        INSTANCE.findCandidates()
        INSTANCE.processProxyList(UrlType.GOOGLE_BOOKS)
    }

}