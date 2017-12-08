package ru.kmorozov.library.data.client.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ContainerServer implements CommandLineRunner {

    private int port;

    @Autowired
    private ServerAdapterInitializer initializer;

    @Override
    public void run(final String... args) {
        port = 5252;
        final EventLoopGroup producer = new NioEventLoopGroup();
        final EventLoopGroup consumer = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(producer, consumer)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initializer);
            System.out.println("Event server started");
            bootstrap.bind(port).sync().channel().closeFuture().sync();

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            producer.shutdownGracefully();
            consumer.shutdownGracefully();
        }
    }
}
