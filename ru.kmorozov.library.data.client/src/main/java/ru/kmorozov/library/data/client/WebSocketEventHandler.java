package ru.kmorozov.library.data.client;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by sbt-morozov-kv on 04.05.2017.
 */

@Component
public class WebSocketEventHandler {

    private final SimpMessagingTemplate websocket;

    @Autowired
    public WebSocketEventHandler(SimpMessagingTemplate websocket) {
        this.websocket = websocket;
    }

    public void sendInfo(Logger logger, String message) {
        logger.info(message);
        this.websocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + "/info", message);
    }
}
