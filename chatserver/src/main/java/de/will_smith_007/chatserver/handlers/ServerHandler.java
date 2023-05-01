package de.will_smith_007.chatserver.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final List<Channel> connectedChannels, authenticatedChannels;

    public ServerHandler(@NotNull List<Channel> connectedChannels,
                         @NotNull List<Channel> authenticatedChannels) {
        this.connectedChannels = connectedChannels;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        final Channel clientChannel = ctx.channel();
        log.info("Ein Client hat die Verbindung aufgebaut. (" + clientChannel.remoteAddress() + ")");
        connectedChannels.add(clientChannel);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        final Channel clientChannel = ctx.channel();
        log.info("Ein Client hat die Verbindung getrennt. (" + clientChannel.remoteAddress() + ")");
        connectedChannels.remove(clientChannel);
        authenticatedChannels.remove(clientChannel);
        clientChannel.close();
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        ctx.close();
    }
}
