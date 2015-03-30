package io.ripc.transport.netty4.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.ReadCompleteListener;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import io.ripc.transport.netty4.listener.ChannelActiveListener;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Listens for events on the Netty IO channel and invokes the appropriate {@link io.ripc.core.EventListener} for that
 * type of event.
 */
public class EventListenerChannelHandler extends ChannelDuplexHandler {

	private static final AtomicLongFieldUpdater<EventListenerChannelHandler> WRITTEN_UPD
			= AtomicLongFieldUpdater.newUpdater(EventListenerChannelHandler.class, "writtenSinceLastFlush");

	private final TcpConnection connection;

	private volatile long writtenSinceLastFlush = 0L;

	private ReadCompleteListener  readCompleteListener;
	private WriteCompleteListener writeCompleteListener;
	private ChannelActiveListener channelActiveListener;

	public EventListenerChannelHandler(TcpConnection connection) {
		this.connection = connection;
	}

	public ReadCompleteListener getReadCompleteListener() {
		return readCompleteListener;
	}

	public void setReadCompleteListener(ReadCompleteListener readCompleteListener) {
		this.readCompleteListener = readCompleteListener;
	}

	public WriteCompleteListener getWriteCompleteListener() {
		return writeCompleteListener;
	}

	public void setWriteCompleteListener(WriteCompleteListener writeCompleteListener) {
		this.writeCompleteListener = writeCompleteListener;
	}

	public ChannelActiveListener getChannelActiveListener() {
		return channelActiveListener;
	}

	public void setChannelActiveListener(ChannelActiveListener channelActiveListener) {
		this.channelActiveListener = channelActiveListener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (null != channelActiveListener) {
			channelActiveListener.channelActive(ctx);
		}
		super.channelActive(ctx);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		super.flush(ctx);
		WRITTEN_UPD.set(this, 0L);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (null != writeCompleteListener) {
			promise.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess() && writeCompleteListener.writeComplete(connection, writtenSinceLastFlush, msg)) {
						ctx.flush();
					}
				}
			});
		}
		super.write(ctx, msg, promise);
		WRITTEN_UPD.incrementAndGet(this);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		if (null != readCompleteListener && readCompleteListener.readComplete(connection)) {
			ctx.close();
		}
	}

}
