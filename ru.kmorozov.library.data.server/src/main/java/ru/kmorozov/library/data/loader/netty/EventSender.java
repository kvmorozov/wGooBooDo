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

    private boolean initIsNotPossible = false;

    public static final EventSender INSTANCE = new EventSender("localhost", 5252);

    private EventSender(final String server, final int port) {
        this.server = server;
        this.port = port;
    }

    private void init() {
        group = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new ClientAdapterInitializer());
        try {
            channel = bootstrap.connect(server, port).sync().channel();

            isInitCompleted = true;
        } catch (final InterruptedException e) {
            group.shutdownGracefully();

            logger.error(e);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            initIsNotPossible = true;
        }
    }

    private void write(final String msg) {
        if (!isInitCompleted)
            if (initIsNotPossible)
                return;
            else
                init();

        if (isInitCompleted) {
            channel.write(msg);
            channel.flush();
        }
    }

    @Override
    public void close() {
        group.shutdownGracefully();
    }

    public void sendInfo(final Logger logger, final String msg) {
        try {
            write(msg);
        } catch (final Exception ex) {
            logger.error(ex.getMessage());
        }

        logger.info(msg);
    }
}