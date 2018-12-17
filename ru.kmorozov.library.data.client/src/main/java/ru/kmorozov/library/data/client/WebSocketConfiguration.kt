package ru.kmorozov.library.data.client

import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

/**
 * Created by sbt-morozov-kv on 04.05.2017.
 */
@Component
@EnableWebSocketMessageBroker
class WebSocketConfiguration : AbstractWebSocketMessageBrokerConfigurer() {

    override fun registerStompEndpoints(registry: StompEndpointRegistry?) {
        registry!!.addEndpoint("/messages").withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry?) {
        registry!!.enableSimpleBroker(MESSAGE_PREFIX)
        registry.setApplicationDestinationPrefixes("/app")
    }

    companion object {

        const val MESSAGE_PREFIX = "/topic"
    }
}
