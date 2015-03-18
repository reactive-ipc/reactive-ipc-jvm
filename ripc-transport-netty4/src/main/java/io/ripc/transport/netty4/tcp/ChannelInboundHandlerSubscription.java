package io.ripc.transport.netty4.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.ripc.core.Specification;
import io.ripc.core.io.Buffer;
import io.ripc.transport.netty4.ByteBufBuffer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class ChannelInboundHandlerSubscription extends ChannelInboundHandlerAdapter implements Subscription {

	private static final AtomicLongFieldUpdater<ChannelInboundHandlerSubscription> PEND_UPD
			= AtomicLongFieldUpdater.newUpdater(ChannelInboundHandlerSubscription.class, "pending");

	private final Channel                             channel;
	private final Subscriber<? super Buffer<ByteBuf>> subscriber;

	private volatile long pending = 0;

	public ChannelInboundHandlerSubscription(Channel channel, Subscriber<? super Buffer<ByteBuf>> subscriber) {
		this.channel = channel;
		this.subscriber = subscriber;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (pending < 1) {
			super.exceptionCaught(ctx, cause);
			return;
		}
		subscriber.onError(cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (pending < 1) {
			super.channelInactive(ctx);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		if (pending < 1) {
			super.channelReadComplete(ctx);
			return;
		}
		subscriber.onComplete();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (pending < 1) {
			super.channelRead(ctx, msg);
			return;
		}

		ByteBuf buf = (ByteBuf) msg;
		try {
			subscriber.onNext(new ByteBufBuffer(buf, false));
			PEND_UPD.decrementAndGet(this);
			//channel.read();
		} catch (Throwable t) {
			subscriber.onError(t);
		}
	}

	@Override
	public void request(long demand) {
		if (!Specification.spec_3_9_verifyPositiveDemand(demand, subscriber)) {
			return;
		}

		if (demand < Long.MAX_VALUE) {
			PEND_UPD.addAndGet(this, demand);
		} else {
			PEND_UPD.set(this, Long.MAX_VALUE);
		}

		channel.read();
	}

	@Override
	public void cancel() {
		synchronized (this) {
			pending = -1;
		}
		channel.close();
	}

}
