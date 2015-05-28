package io.ripc.transport.netty4;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.reactivestreams.Subscriber;

/**
 * Created by jbrisbin on 5/28/15.
 */
public final class SubscriberListener implements ChannelFutureListener {

	private final Subscriber<? super Void> subscriber;

	public SubscriberListener(Subscriber<? super Void> subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (future.isSuccess()) {
			subscriber.onComplete();
		} else {
			subscriber.onError(future.cause());
		}
	}

}
