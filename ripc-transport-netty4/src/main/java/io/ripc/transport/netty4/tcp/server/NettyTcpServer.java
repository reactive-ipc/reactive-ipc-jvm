package io.ripc.transport.netty4.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.ripc.core.NamedDaemonThreadFactory;
import io.ripc.protocol.tcp.ConnectionHandler;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NettyTcpServer extends ChannelInitializer<SocketChannel> {

	private final ServerBootstrap            bootstrap;
	private final ConnectionHandler<ByteBuf> handler;

	public NettyTcpServer(ServerBootstrap bootstrap,
	                      ConnectionHandler<ByteBuf> handler) {
		this.bootstrap = bootstrap;
		this.handler = handler;
	}

	public static NettyTcpServer listen(int port, ConnectionHandler<ByteBuf> handler) {
		ServerBootstrap b = new ServerBootstrap();

		int threads = Runtime.getRuntime().availableProcessors();
		EventLoopGroup ioGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-io"));
		EventLoopGroup workerGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-worker"));
		b.group(ioGroup, workerGroup);

		b.channel(NioServerSocketChannel.class);

		NettyTcpServer server = new NettyTcpServer(b, handler);
		b.childHandler(server);

		b.bind(port);

		return server;
	}

	public void shutdown() {
		bootstrap.group().shutdownGracefully();
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.config().setAutoRead(false);
		ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);

		ch.pipeline().addLast(new LoggingHandler());

		NettyTcpServerConnection conn = new NettyTcpServerConnection(ch);
		handler.handle(conn);
	}

}
