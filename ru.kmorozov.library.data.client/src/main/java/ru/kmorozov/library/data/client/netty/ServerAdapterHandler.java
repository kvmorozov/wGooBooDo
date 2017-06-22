package ru.kmorozov.library.data.client.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.client.WebSocketEventHandler;

@Component
@ChannelHandler.Sharable
public class ServerAdapterHandler extends SimpleChannelInboundHandler<String> {

    private static final ChannelGroup channels = new DefaultChannelGroup("containers", GlobalEventExecutor.INSTANCE);

    @Autowired
    private WebSocketEventHandler eventHandler;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[START] New Container has been initialzed");
        channels.add(ctx.channel());
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[END] A Container has been removed");
        channels.remove(ctx.channel());
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        channelRead0(ctx, msg.toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (eventHandler != null)
            eventHandler.sendInfo(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext arg0) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("channelWritabilityChanged");
    }
}
