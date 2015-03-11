package io.ripc.transport.netty4.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.ripc.core.NamedDaemonThreadFactory;
import io.ripc.protocol.tcp.Connection;
import io.ripc.protocol.tcp.ConnectionPublisher;
import io.ripc.transport.netty4.tcp.ChannelInitializerSubscription;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NettyTcpServer extends ChannelInitializer<SocketChannel> implements ConnectionPublisher<ByteBuf> {

	private final List<ChannelInitializerSubscription> subscriptions = new ArrayList<>();

	private final ServerBootstrap bootstrap;

	public NettyTcpServer(ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public static ConnectionPublisher<ByteBuf> listen(int port) {
		ServerBootstrap b = new ServerBootstrap();

		int threads = Runtime.getRuntime().availableProcessors();
		EventLoopGroup ioGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-io"));
		EventLoopGroup workerGroup = new NioEventLoopGroup(threads, new NamedDaemonThreadFactory("netty-worker"));
		b.group(ioGroup, workerGroup);

		b.channel(NioServerSocketChannel.class);

		NettyTcpServer server = new NettyTcpServer(b);
		b.childHandler(server);

		b.bind(port);

		return server;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.config().setAutoRead(false);
		ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);

		synchronized (subscriptions) {
			for (ChannelInitializerSubscription sub : subscriptions) {
				ch.pipeline().addLast(sub);
			}
		}
	}

	@Override
	public void subscribe(Subscriber<? super Connection<ByteBuf>> s) {
		ChannelInitializerSubscription sub = new ChannelInitializerSubscription(s);
		synchronized (subscriptions) {
			subscriptions.add(sub);
		}
		s.onSubscribe(sub);
	}
}
