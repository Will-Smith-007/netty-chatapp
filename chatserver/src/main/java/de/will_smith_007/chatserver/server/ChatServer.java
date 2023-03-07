package de.will_smith_007.chatserver.server;

import de.will_smith_007.chatserver.handlers.ChatMessageHandler;
import de.will_smith_007.chatserver.handlers.ServerHandler;
import de.will_smith_007.chatserver.handlers.TokenAuthHandler;
import de.will_smith_007.chatserver.logger.SimpleLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

@AllArgsConstructor
public class ChatServer {

    private int port;
    private static final boolean IS_EPOLL = Epoll.isAvailable();
    private volatile List<Channel> channels, authenticatedChannels;
    private SimpleLogger simpleLogger;

    public void runServer() throws Exception {
        simpleLogger.log(SimpleLogger.Level.INFO, "Chat Server wird gestartet...");

        final EventLoopGroup bossGroup = (IS_EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup());
        final EventLoopGroup workerGroup = (IS_EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup());
        try {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel((IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) {
                            final ChannelPipeline channelPipeline = ch.pipeline();

                            final TokenAuthHandler tokenAuthHandler = new TokenAuthHandler(
                                    "SuperSecretToken",
                                    simpleLogger,
                                    authenticatedChannels);

                            channelPipeline.addFirst(tokenAuthHandler);
                            channelPipeline.addLast(new ServerHandler(simpleLogger, channels, authenticatedChannels));
                            channelPipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            channelPipeline.addLast(new ChatMessageHandler(channels, authenticatedChannels));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            simpleLogger.log(SimpleLogger.Level.INFO, "Chat Server wurde gestartet.");

            channelFuture.channel().closeFuture().sync();
        } finally {
            simpleLogger.log(SimpleLogger.Level.INFO, "Chat Server wurde beendet.");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
