package ru.kmorozov.library.data.client.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.utils.Logger;
import ru.kmorozov.library.data.client.WebSocketEventHandler;

import java.util.Collection;

@Component
@Sharable
public class ServerAdapterHandler extends SimpleChannelInboundHandler<String> {

    protected static final Logger logger = Logger.getLogger(ServerAdapterHandler.class);
    private static final Collection channels = new DefaultChannelGroup("containers", GlobalEventExecutor.INSTANCE);

    @Autowired
    private WebSocketEventHandler eventHandler;

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("[START] New Container has been initialzed");
        channels.add(ctx.channel());
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("[END] A Container has been removed");
        channels.remove(ctx.channel());
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        channelRead0(ctx, msg.toString());
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final String msg) {
        if (null != eventHandler)
            eventHandler.sendInfo(msg);

        logger.info(msg);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext arg0) {
        // TODO Auto-generated method stub
        System.out.println("channelWritabilityChanged");
    }
}
