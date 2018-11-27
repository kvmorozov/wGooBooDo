package ru.kmorozov.library.data.loader.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.kmorozov.gbd.logger.Logger;

import java.io.Closeable;

public final class EventSender implements Closeable {

    private static final Logger logger = Logger.getLogger(EventSender.class);

    private final String server;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;
    private boolean isInitCompleted;

    private boolean initIsNotPossible;

    public static final EventSender INSTANCE = new EventSender("localhost", 5252);

    private EventSender(String server, int port) {
        this.server = server;
        this.port = port;
    }

    private void init() {
        this.group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap().group(this.group)
                .channel(NioSocketChannel.class)
                .handler(new ClientAdapterInitializer());
        try {
            this.channel = bootstrap.connect(this.server, this.port).sync().channel();

            this.isInitCompleted = true;
        } catch (InterruptedException e) {
            this.group.shutdownGracefully();

            EventSender.logger.error(e);
        } catch (final Exception ex) {
            EventSender.logger.error(ex.getMessage());
            this.initIsNotPossible = true;
        }
    }

    private void write(String msg) {
        if (!this.isInitCompleted)
            if (this.initIsNotPossible)
                return;
            else
                this.init();

        if (this.isInitCompleted) {
            this.channel.write(msg);
            this.channel.flush();
        }
    }

    @Override
    public void close() {
        this.group.shutdownGracefully();
    }

    public void sendInfo(Logger logger, String msg) {
        try {
            this.write(msg);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        logger.info(msg);
    }
}