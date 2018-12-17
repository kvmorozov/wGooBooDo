package ru.kmorozov.library.data.client.netty

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.client.WebSocketEventHandler

@Component
@Sharable
class ServerAdapterHandler : SimpleChannelInboundHandler<String>() {

    @Autowired
    private val eventHandler: WebSocketEventHandler? = null

    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        println("[START] New Container has been initialzed")
        channels.add(ctx!!.channel())
        super.handlerAdded(ctx)
    }

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        println("[END] A Container has been removed")
        channels.remove(ctx!!.channel())
        super.handlerRemoved(ctx)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        channelRead0(ctx, msg.toString())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        eventHandler?.sendInfo(msg)

        logger.info(msg)
    }

    override fun channelReadComplete(arg0: ChannelHandlerContext) {
        // TODO Auto-generated method stub
    }

    override fun channelWritabilityChanged(arg0: ChannelHandlerContext) {
        // TODO Auto-generated method stub
        println("channelWritabilityChanged")
    }

    companion object {

        protected val logger = Logger.getLogger(ServerAdapterHandler::class.java)
        private val channels = DefaultChannelGroup("containers", GlobalEventExecutor.INSTANCE)
    }
}
