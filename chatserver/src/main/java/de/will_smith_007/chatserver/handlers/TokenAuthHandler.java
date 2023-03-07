package de.will_smith_007.chatserver.handlers;

import de.will_smith_007.chatserver.logger.SimpleLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TokenAuthHandler extends MessageToMessageDecoder<ByteBuf> {

    private final String requiredToken;
    private final SimpleLogger simpleLogger;
    private final List<Channel> authenticatedChannels;

    public TokenAuthHandler(@NotNull String requiredToken,
                            @NotNull SimpleLogger simpleLogger,
                            @NotNull List<Channel> authenticatedChannels) {
        this.requiredToken = requiredToken;
        this.simpleLogger = simpleLogger;
        this.authenticatedChannels = authenticatedChannels;
    }

    @Override
    protected void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf msg, List<Object> out) {
        final Channel clientChannel = ctx.channel();
        final String message = msg.toString(StandardCharsets.UTF_8);
        if (!authenticatedChannels.contains(clientChannel)) {
            if (message.equals(requiredToken)) {
                authenticatedChannels.add(clientChannel);
                simpleLogger.log(SimpleLogger.Level.INFO, "Ein Client hat die Verbindung verifiziert. (" + clientChannel.remoteAddress() + ")");
                ReferenceCountUtil.release(message);
            } else {
                simpleLogger.log(SimpleLogger.Level.WARN, "Ein Client konnte nicht verifiziert werden. (" + clientChannel.remoteAddress() + ")");
                ctx.close();
            }
        } else {
            out.add(message);
        }
    }
}
