package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.ripc.core.io.Buffer;
import io.ripc.protocol.tcp.Connection;
import io.ripc.transport.netty4.tcp.ChannelInboundHandlerSubscription;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Represents a Netty Channel.
 */
public class NettyTcpServerConnection implements Connection<ByteBuf> {

	private final Channel channel;

	public NettyTcpServerConnection(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void write(Publisher<Buffer<ByteBuf>> data) {
		data.subscribe(new WriteSubscriber());
	}

	@Override
	public void subscribe(Subscriber<? super Buffer<ByteBuf>> s) {
		ChannelInboundHandlerSubscription sub = new ChannelInboundHandlerSubscription(channel, s);
		s.onSubscribe(sub);
		channel.pipeline()
		       .addLast(sub);
	}

	@Override
	public String toString() {
		return "NettyTcpServerConnection{" +
		       "channel=" + channel +
		       '}';
	}

	private final class WriteSubscriber implements Subscriber<Buffer<ByteBuf>> {
		Subscription subscription;

		@Override
		public void onSubscribe(Subscription s) {
			if (null != subscription) {
				s.cancel();
				return;
			}
			(subscription = s).request(1);
		}

		@Override
		public void onNext(Buffer<ByteBuf> buffer) {
			channel.write(buffer.get());
			// This causes a StackOverflowError
			//subscription.request(1);
			// This doesn't
			channel.eventLoop().execute(() -> subscription.request(1));
		}

		@Override
		public void onError(Throwable t) {

		}

		@Override
		public void onComplete() {
			channel.flush();
		}
	}

}
