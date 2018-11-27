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
    public void run(String... args) {
        this.port = 5252;
        EventLoopGroup producer = new NioEventLoopGroup();
        EventLoopGroup consumer = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(producer, consumer)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(this.initializer);
            System.out.println("Event server started");
            bootstrap.bind(this.port).sync().channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.shutdownGracefully();
            consumer.shutdownGracefully();
        }
    }
}
