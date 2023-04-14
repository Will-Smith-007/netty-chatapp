package de.will_smith_007.chatserver.server;

import de.will_smith_007.chatserver.handlers.ChatMessageHandler;
import de.will_smith_007.chatserver.handlers.ServerHandler;
import de.will_smith_007.chatserver.handlers.TokenAuthHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

@AllArgsConstructor
public class ChatServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private int port;
    private static final boolean IS_EPOLL = Epoll.isAvailable();
    private volatile List<Channel> channels, authenticatedChannels;

    public void runServer() throws Exception {
        LOGGER.info("Chat Server wird gestartet...");

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
                                    authenticatedChannels);

                            channelPipeline.addFirst(tokenAuthHandler);
                            channelPipeline.addLast(new ServerHandler(channels, authenticatedChannels));
                            channelPipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            channelPipeline.addLast(new ChatMessageHandler(channels, authenticatedChannels));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            LOGGER.info("Chat Server wurde gestartet.");

            channelFuture.channel().closeFuture().sync();
        } finally {
            LOGGER.info("Chat Server wurde beendet.");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
