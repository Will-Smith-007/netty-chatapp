package de.will_smith_007.chatserver.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatMessageHandler extends SimpleChannelInboundHandler<String> {

    private final List<Channel> channels;
    private final List<Channel> authenticatedChannels;

    public ChatMessageHandler(@NotNull List<Channel> channels,
                              @NotNull List<Channel> authenticatedChannels) {
        this.channels = channels;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println(msg);
        channels.forEach(channel -> {
            if (authenticatedChannels.contains(channel)) {
                channel.writeAndFlush(msg, channel.voidPromise());
            } else {
                channel.writeAndFlush("Authentifizierung fehlgeschlagen, Verbindung wurde unterbrochen.");
                ctx.close();
            }
        });
    }
}
