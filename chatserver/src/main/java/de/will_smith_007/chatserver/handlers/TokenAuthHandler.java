package de.will_smith_007.chatserver.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class TokenAuthHandler extends MessageToMessageDecoder<ByteBuf> {

    private final String requiredToken;
    private final List<Channel> authenticatedChannels;

    public TokenAuthHandler(@NotNull String requiredToken,
                            @NotNull List<Channel> authenticatedChannels) {
        this.requiredToken = requiredToken;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    protected void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf msg, List<Object> out) {
        final Channel clientChannel = ctx.channel();
        final String message = msg.toString(StandardCharsets.UTF_8);

        // If client is authenticated, forward this message to the next channel handler.
        if (authenticatedChannels.contains(clientChannel)) {
            out.add(message);
            return;
        }

        // If client isn't authenticated check the incoming message for the required token.
        if (message.equals(requiredToken)) {
            authenticatedChannels.add(clientChannel);
            log.info("Ein Client hat die Verbindung verifiziert. (" + clientChannel.remoteAddress() + ")");
            ReferenceCountUtil.release(message);
        } else {
            log.warn("Ein Client konnte nicht verifiziert werden. (" + clientChannel.remoteAddress() + ")");
            clientChannel.writeAndFlush("Verbindung konnte nicht verifiziert werden.");
            ctx.close();
        }
    }
}
