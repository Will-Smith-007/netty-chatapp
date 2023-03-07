package de.will_smith_007.chatclient.client;

import de.will_smith_007.chatclient.handlers.ChatMessageHandler;
import de.will_smith_007.chatclient.logger.SimpleLogger;
import de.will_smith_007.chatclient.terminal.Terminal;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class Client {

    private final String host;
    private final int port;
    private final String requiredToken;
    private static final boolean IS_EPOLL = Epoll.isAvailable();

    private final SimpleLogger simpleLogger;

    public void connect() {
        simpleLogger.log(SimpleLogger.Level.INFO, "Verbinde mit Chat Server...");
        final EventLoopGroup workerGroup = (IS_EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup());
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel((IS_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) {
                            final ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            channelPipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            channelPipeline.addLast(new ChatMessageHandler());
                        }
                    });

            final ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            final Channel clientChannel = channelFuture.channel();

            //Send required verification token to read and write.
            final ByteBuf byteBuf = clientChannel.alloc().buffer();
            byteBuf.writeCharSequence(requiredToken, StandardCharsets.UTF_8);
            clientChannel.writeAndFlush(byteBuf);

            simpleLogger.log(SimpleLogger.Level.INFO, "Erfolgreich mit Chat Server verbunden.");

            //Terminal initialization must be done before waiting for clientChannel close.
            final Terminal terminal = new Terminal(clientChannel);
            terminal.startTerminal();

            //Wait for client channel close.
            clientChannel.closeFuture().sync();
        } catch (InterruptedException interruptedException) {
            System.exit(0);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
