package io.ripc.transport.netty4.tcp.server;

import io.netty.channel.Channel;
import io.ripc.core.DemandCalculator;
import io.ripc.core.EventListener;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.ReadCompleteListener;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import io.ripc.transport.netty4.tcp.ChannelInboundHandlerSubscription;
import io.ripc.transport.netty4.tcp.EventListenerChannelHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Represents a Reactive IPC {@code TcpConnection}.
 */
public class NettyTcpServerConnection implements TcpConnection {

	private static final AtomicLongFieldUpdater<WriteSubscriber> PENDING_UPD
			= AtomicLongFieldUpdater.newUpdater(WriteSubscriber.class, "pending");

	private final Channel       channel;
	private final ReadPublisher readPublisher;

	private WriteSubscriber writeSubscriber;

	public NettyTcpServerConnection(Channel channel) {
		this.channel = channel;
		this.readPublisher = new ReadPublisher(channel);
	}

	@Override
	public TcpConnection addListener(EventListener listener) {
		EventListenerChannelHandler handler =
				channel.pipeline().get(EventListenerChannelHandler.class);
		if (null == handler) {
			handler = new EventListenerChannelHandler(this);
			channel.pipeline().addLast(handler);
		}

		Class<?> listenerType = listener.getClass();

		// Assign WriteCompleteListener that will be notified of successful writes.
		if (WriteCompleteListener.class.isAssignableFrom(listenerType)) {
			handler.setWriteCompleteListener((WriteCompleteListener) listener);
		}

		// Assign a ReadCompleteListener that will be notified when all data has been read.
		if (ReadCompleteListener.class.isAssignableFrom(listenerType)) {
			handler.setReadCompleteListener((ReadCompleteListener) listener);
		}

		return this;
	}

	@Override
	public Publisher<Object> reader() {
		return readPublisher;
	}

	@Override
	public TcpConnection writer(Publisher<Object> writer) {
		if (null != writeSubscriber) {
			throw new IllegalStateException("A writer has already been set on this connection");
		}

		DemandCalculator demandCalculator = DemandCalculator.class.isAssignableFrom(writer.getClass())
		                                    ? (DemandCalculator) writer
		                                    : null;
		this.writeSubscriber = new WriteSubscriber(demandCalculator);
		writer.subscribe(writeSubscriber);
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

					if (toRequest == Long.MAX_VALUE && pending != Long.MAX_VALUE) {
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
			// Request for any writes right away.
			subscriptionRequest.run();
		}

		@Override
		public void onNext(Object msg) {
			// Write to the channel for every onNext signal.
			channel.write(msg);
			// Decrement pending counter to show we've handled this write.
			PENDING_UPD.decrementAndGet(this);
			// We'll get a StackOverflowError if we don't schedule this request.
			channel.eventLoop().execute(subscriptionRequest);
		}

		@Override
		public void onError(Throwable t) {
			channel.close();
		}

		@Override
		public void onComplete() {
			// Write completes always result in a flush.
			channel.flush();
			// This is a terminal state, so close the underlying channel.
			channel.close();
		}
	}

}
