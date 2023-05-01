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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class ChatServer {

    private int port;
    private static final boolean IS_EPOLL = Epoll.isAvailable();
    private volatile List<Channel> connectedChannels, authenticatedChannels;

    public void runServer() throws Exception {
        log.info("Chat Server wird gestartet...");

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
                            channelPipeline.addLast(new ServerHandler(connectedChannels, authenticatedChannels));
                            channelPipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            channelPipeline.addLast(new ChatMessageHandler(connectedChannels, authenticatedChannels));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("Chat Server wurde gestartet.");

            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
