package ru.kmorozov.library.data.loader.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

public class EventSender implements Closeable {

    private static final Logger logger = Logger.getLogger(EventSender.class);

    private String server;
    private int port;
    private EventLoopGroup group;
    private Channel channel;
    private boolean isInitCompleted = false;

    public static EventSender INSTANCE = new EventSender("localhost", 5252);

    private EventSender(String server, int port) {
        this.server = server;
        this.port = port;
    }

    private void init() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new ClientAdapterInitializer());
        try {
            channel = bootstrap.connect(server, port).sync().channel();

            isInitCompleted = true;
        } catch (InterruptedException e) {
            group.shutdownGracefully();

            logger.error(e);
        }
    }

    private void write(String msg) {
        if (!isInitCompleted)
            init();

        if (isInitCompleted)
            channel.write(msg);
    }

    @Override
    public void close() throws IOException {
        group.shutdownGracefully();
    }

    public void sendInfo(Logger logger, String msg) {
        write(msg);
        logger.info(msg);
    }
}