package ru.kmorozov.library.data.loader.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientAdapterHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // TODO Auto-generated method stub
        this.channelRead0(ctx, msg.toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Msg received: " + msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub

    }

}