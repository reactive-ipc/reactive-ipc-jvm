package io.ripc.transport.netty4.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.ripc.core.Specification;
import io.ripc.protocol.tcp.Connection;
import io.ripc.transport.netty4.tcp.server.NettyTcpServerConnection;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class ChannelInitializerSubscription extends ChannelInitializer<SocketChannel> implements Subscription {

	private final Subscriber<? super Connection<ByteBuf>> subscriber;

	private volatile long pending = 0l;

	public ChannelInitializerSubscription(Subscriber<? super Connection<ByteBuf>> subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		if (pending < 1) {
			return;
		}
		NettyTcpServerConnection conn = new NettyTcpServerConnection(ch);
		try {
			subscriber.onNext(conn);
			synchronized (this) {
				pending--;
			}
		} catch (Throwable t) {
			subscriber.onError(t);
		}
	}

	@Override
	public void request(long demand) {
		if (!Specification.spec_3_9_verifyPositiveDemand(demand, subscriber)) {
			return;
		}
		synchronized (this) {
			pending += demand;
		}
	}

	@Override
	public void cancel() {
		synchronized (this) {
			pending = -1;
		}

	}

}
