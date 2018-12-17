package ru.kmorozov.library.data.loader.netty

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import ru.kmorozov.gbd.logger.Logger

import java.io.Closeable

class EventSender private constructor(private val server: String, private val port: Int) : Closeable {
    private var group: EventLoopGroup? = null
    private var channel: Channel? = null
    private var isInitCompleted: Boolean = false

    private var initIsNotPossible: Boolean = false

    private fun init() {
        group = NioEventLoopGroup()
        val bootstrap = Bootstrap().group(group!!)
                .channel(NioSocketChannel::class.java)
                .handler(ClientAdapterInitializer())
        try {
            channel = bootstrap.connect(server, port).sync().channel()

            isInitCompleted = true
        } catch (e: InterruptedException) {
            group!!.shutdownGracefully()

            logger.error(e)
        } catch (ex: Exception) {
            logger.error(ex.message!!)
            initIsNotPossible = true
        }

    }

    private fun write(msg: String) {
        if (!isInitCompleted)
            if (initIsNotPossible)
                return
            else
                init()

        if (isInitCompleted) {
            channel!!.write(msg)
            channel!!.flush()
        }
    }

    override fun close() {
        group!!.shutdownGracefully()
    }

    fun sendInfo(logger: Logger, msg: String) {
        try {
            write(msg)
        } catch (ex: Exception) {
            logger.error(ex.message!!)
        }

        logger.info(msg)
    }

    companion object {

        private val logger = Logger.getLogger(EventSender::class.java)

        val INSTANCE = EventSender("localhost", 5252)
    }
}