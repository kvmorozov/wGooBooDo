package ru.kmorozov.library.data.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/**
 * Created by sbt-morozov-kv on 04.05.2017.
 */

@Component
class WebSocketEventHandler @Autowired
constructor(private val websocket: SimpMessagingTemplate) {

    fun sendInfo(message: String) {
        this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/info", message)
    }
}
