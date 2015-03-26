package io.ripc.transport.netty4.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.ripc.protocol.tcp.TcpConnectionHandler;
import io.ripc.transport.netty4.NamedDaemonThreadFactory;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NettyTcpServer {

	private final ServerBootstrap bootstrap;

	public NettyTcpServer(ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public static NettyTcpServer listen(int port, TcpConnectionHandler handler) {
		ServerBootstrap b = new ServerBootstrap();

		int threads = Runtime.getRuntime().availableProcessors();
		EventLoopGroup ioGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-io"));
		EventLoopGroup workerGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-worker"));
		b.group(ioGroup, workerGroup);

		b.channel(NioServerSocketChannel.class);

		NettyTcpServer server = new NettyTcpServer(b);
		b.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.config().setAutoRead(false);
				ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);

				ch.pipeline().addLast(new LoggingHandler());

				NettyTcpServerConnection conn = new NettyTcpServerConnection(ch);
				handler.handle(conn);
			}
		});

		b.bind(port);

		return server;
	}

	public void shutdown() {
		bootstrap.group().shutdownGracefully();
	}

}
