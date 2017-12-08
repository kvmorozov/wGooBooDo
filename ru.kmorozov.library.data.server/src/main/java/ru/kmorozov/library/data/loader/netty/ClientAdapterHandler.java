package ru.kmorozov.library.data.loader.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientAdapterHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg)
            throws Exception {
        // TODO Auto-generated method stub
        channelRead0(ctx, msg.toString());
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final String msg) {
        System.out.println("Msg received: " + msg);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub

    }

}