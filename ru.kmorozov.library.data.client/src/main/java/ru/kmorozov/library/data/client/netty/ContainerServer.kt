package ru.kmorozov.library.data.client.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ContainerServer : CommandLineRunner {

    private var port: Int = 0

    @Autowired
    private val initializer: ServerAdapterInitializer? = null

    override fun run(vararg args: String) {
        port = 5252
        val producer = NioEventLoopGroup()
        val consumer = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()
                    .group(producer, consumer)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(initializer!!)
            println("Event server started")
            bootstrap.bind(port).sync().channel().closeFuture().sync()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            producer.shutdownGracefully()
            consumer.shutdownGracefully()
        }
    }
}
