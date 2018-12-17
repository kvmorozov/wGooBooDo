package ru.kmorozov.library.data.loader.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientAdapterHandler : SimpleChannelInboundHandler<String>() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // TODO Auto-generated method stub
        channelRead0(ctx, msg.toString())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        println("Msg received: $msg")
    }

    override fun channelReadComplete(arg0: ChannelHandlerContext) {
        // TODO Auto-generated method stub

    }

    override fun channelWritabilityChanged(arg0: ChannelHandlerContext) {
        // TODO Auto-generated method stub

    }

}