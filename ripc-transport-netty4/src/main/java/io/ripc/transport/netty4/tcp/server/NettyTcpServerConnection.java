package io.ripc.transport.netty4.tcp.server;

import io.netty.channel.Channel;
import io.ripc.core.DemandCalculator;
import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.protocol.tcp.TcpConnectionEventHandler;
import io.ripc.transport.netty4.tcp.ChannelInboundHandlerSubscription;
import io.ripc.transport.netty4.tcp.NettyChannelTcpConnectionEventHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Represents a Netty Channel.
 */
public class NettyTcpServerConnection implements TcpConnection {

	private static final AtomicLongFieldUpdater<WriteSubscriber> PENDING_UPD
			= AtomicLongFieldUpdater.newUpdater(WriteSubscriber.class, "pending");

	private final Channel       channel;
	private final ReadPublisher readPublisher;

	private TcpConnectionEventHandler eventHandler;

	public NettyTcpServerConnection(Channel channel) {
		this.channel = channel;
		this.readPublisher = new ReadPublisher(channel);
	}

	@Override
	public TcpConnection eventHandler(TcpConnectionEventHandler eventHandler) {
		if (null != this.eventHandler) {
			throw new IllegalArgumentException(TcpConnectionEventHandler.class.getSimpleName()
			                                   + " already set on this connection");
		}
		this.eventHandler = eventHandler;
		channel.pipeline().addLast(new NettyChannelTcpConnectionEventHandler(eventHandler, this));
		return this;
	}

	@Override
	public Publisher<?> reader() {
		return readPublisher;
	}

	@Override
	public TcpConnection writer(Publisher<?> sink) {
		DemandCalculator demandCalculator = DemandCalculator.class.isAssignableFrom(sink.getClass())
		                                    ? (DemandCalculator) sink
		                                    : null;
		sink.subscribe(new WriteSubscriber(demandCalculator));
		return this;
	}

	@Override
	public String toString() {
		return "NettyTcpServerConnection{" +
		       "channel=" + channel +
		       '}';
	}

	private final static class ReadPublisher implements Publisher<Object> {
		private final Channel channel;

		public ReadPublisher(Channel channel) {
			this.channel = channel;
		}

		@Override
		public void subscribe(Subscriber<? super Object> subscriber) {
			ChannelInboundHandlerSubscription sub = new ChannelInboundHandlerSubscription(channel, subscriber);
			subscriber.onSubscribe(sub);
			channel.pipeline().addLast(sub);
		}
	}

	private final class WriteSubscriber implements Subscriber<Object> {
		private final Runnable subscriptionRequest;

		private Subscription subscription;

		volatile long pending = 0L;

		private WriteSubscriber(DemandCalculator demandCalculator) {
			this.subscriptionRequest = new Runnable() {
				@Override
				public void run() {
					final long toRequest;
					if (null != demandCalculator) {
						toRequest = demandCalculator.calculateDemand(pending);
					} else {
						toRequest = 1L;
					}

					if (toRequest == Long.MAX_VALUE) {
						PENDING_UPD.set(WriteSubscriber.this, Long.MAX_VALUE);
						subscription.request(Long.MAX_VALUE);
					} else if (toRequest > 0) {
						PENDING_UPD.addAndGet(WriteSubscriber.this, toRequest);
						subscription.request(pending);
					}
				}
			};
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			if (null != this.subscription) {
				subscription.cancel();
				return;
			}

			this.subscription = subscription;
			subscriptionRequest.run();
		}

		@Override
		public void onNext(Object msg) {
			channel.write(msg);
			PENDING_UPD.decrementAndGet(this);
			channel.eventLoop().execute(subscriptionRequest);
		}

		@Override
		public void onError(Throwable t) {
			if (null != eventHandler) {
				eventHandler.onError(NettyTcpServerConnection.this, t);
			}
		}

		@Override
		public void onComplete() {
			channel.flush();
			channel.close();
		}
	}

}
