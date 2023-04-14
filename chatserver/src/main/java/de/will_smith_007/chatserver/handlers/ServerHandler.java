package de.will_smith_007.chatserver.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    private final List<Channel> channels;

    //private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final List<Channel> authenticatedChannels;

    public ServerHandler(@NotNull List<Channel> channels,
                         @NotNull List<Channel> authenticatedChannels) {
        this.channels = channels;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        final Channel clientChannel = ctx.channel();
        LOGGER.info("Ein Client hat die Verbindung aufgebaut. (" + clientChannel.remoteAddress() + ")");
        channels.add(clientChannel);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        final Channel clientChannel = ctx.channel();
        LOGGER.info("Ein Client hat die Verbindung getrennt. (" + clientChannel.remoteAddress() + ")");
        channels.remove(clientChannel);
        authenticatedChannels.remove(clientChannel);
        clientChannel.close();
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        ctx.close();
    }
}
