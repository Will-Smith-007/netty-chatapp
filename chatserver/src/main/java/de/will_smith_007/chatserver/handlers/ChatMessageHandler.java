package de.will_smith_007.chatserver.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatMessageHandler extends SimpleChannelInboundHandler<String> {

    private final List<Channel> connectedChannels, authenticatedChannels;

    public ChatMessageHandler(@NotNull List<Channel> connectedChannels,
                              @NotNull List<Channel> authenticatedChannels) {
        this.connectedChannels = connectedChannels;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println(msg);
        connectedChannels.forEach(clientChannel -> {
            if (authenticatedChannels.contains(clientChannel)) {
                clientChannel.writeAndFlush(msg, clientChannel.voidPromise());
            } else {
                clientChannel.writeAndFlush("Authentifizierung fehlgeschlagen, Verbindung wurde unterbrochen.");
                ctx.close();
            }
        });
    }
}
