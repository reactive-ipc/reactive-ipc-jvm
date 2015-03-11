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

/**
 * Created by jbrisbin on 3/10/15.
 */
public class ChannelInboundHandlerSubscription extends ChannelInboundHandlerAdapter implements Subscription {

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
			return;
		}
		subscriber.onComplete();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		if (pending < 1) {
			super.channelReadComplete(ctx);
		}
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
			synchronized (this) {
				pending--;
			}
			channel.read();
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
			if (demand < Long.MAX_VALUE) {
				pending += demand;
			} else {
				pending = Long.MAX_VALUE;
			}
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
