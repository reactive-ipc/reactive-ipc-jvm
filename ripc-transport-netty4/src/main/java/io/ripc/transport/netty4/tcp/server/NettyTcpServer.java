package io.ripc.transport.netty4.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.ripc.core.Handler;
import io.ripc.core.Interceptor;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.transport.netty4.NamedDaemonThreadFactory;

/**
 * An implementation of {@link io.ripc.protocol.tcp.TcpServer} that uses Netty.
 */
public class NettyTcpServer<C> implements TcpServer<C> {

	private final int             port;
	private final Interceptor     interceptor;
	private final ServerBootstrap bootstrap;

	private ChannelFuture startFuture;

	private Handler handler;

	public NettyTcpServer(int port, Interceptor interceptor, ServerBootstrap bootstrap) {
		this.port = port;
		this.interceptor = interceptor;
		this.bootstrap = bootstrap;
	}

	public static NettyTcpServer<TcpConnection> listen(int port) {
		int threads = Runtime.getRuntime().availableProcessors();

		ServerBootstrap b = new ServerBootstrap()
				.group(new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-io")),
				       new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-worker")))
				.channel(NioServerSocketChannel.class);

		return new NettyTcpServer<>(port, null, b);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <NEWC> TcpServer<NEWC> intercept(Interceptor<C, NEWC> interceptor) {
		return new NettyTcpServer<>(port, interceptor, bootstrap);
	}

	@Override
	public NettyTcpServer<C> handler(Handler<C> handler) {
		if (null != this.handler) {
			throw new IllegalArgumentException("A handler has already been set on this TcpServer");
		}
		this.handler = handler;

		this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@SuppressWarnings("unchecked")
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.config().setAutoRead(false);
				ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);

				ch.pipeline().addLast(new LoggingHandler());

				NettyTcpServerConnection conn = new NettyTcpServerConnection(ch);
				if (null != interceptor) {
					handler.handle((C) interceptor.intercept(conn));
				} else {
					handler.handle((C) conn);
				}
			}
		});

		return this;
	}

	@Override
	public void start() {
		if (null != startFuture) {
			return;
		}
		startFuture = bootstrap.bind(port);
	}

	public void shutdown() {
		bootstrap.group().shutdownGracefully();
	}

}
