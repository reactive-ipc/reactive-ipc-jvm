package io.ripc.transport.netty4.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.ripc.protocol.tcp.TcpHandler;
import io.ripc.protocol.tcp.TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Netty4TcpServer<R, W> extends TcpServer<R, W> {

    private static final Logger logger = LoggerFactory.getLogger(Netty4TcpServer.class);

    private int port;
    private ServerBootstrap bootstrap;
    private ChannelFuture bindFuture;

    protected Netty4TcpServer(int port) {
        this.port = port;
        bootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class);
    }

    @Override
    protected Netty4TcpServer<R, W> doStart(final TcpHandler<R, W> handler) {
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast("server_handler", new ChannelToConnectionBridge<>(handler));
            }
        });

        try {
            bindFuture = bootstrap.bind(port).sync();
            if (!bindFuture.isSuccess()) {
                throw new RuntimeException(bindFuture.cause());
            }
            SocketAddress localAddress = bindFuture.channel().localAddress();
            if (localAddress instanceof InetSocketAddress) {
                port = ((InetSocketAddress) localAddress).getPort();
                logger.info("Started server at port: " + port);
            }

        } catch (InterruptedException e) {
            logger.error("Error waiting for binding server port: " + port, e);
        }

        return this;
    }

    @Override
    public void awaitShutdown() {
        try {
            bindFuture.channel().closeFuture().await();
        } catch (InterruptedException e) {
            Thread.interrupted(); // Reset the interrupted status
            logger.error("Interrupted while waiting for the server socket to close.", e);
        }
    }

    @Override
    public boolean doShutdown() {
        try {
            bindFuture.channel().close().sync();
            return true;
        } catch (InterruptedException e) {
            logger.error("Failed to shutdown the server.", e);
            return false;
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    public static <R, W> TcpServer<R, W> create(int port) {
        return new Netty4TcpServer<>(port);
    }

}
