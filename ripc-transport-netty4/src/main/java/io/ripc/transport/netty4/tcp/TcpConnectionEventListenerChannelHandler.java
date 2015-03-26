package io.ripc.transport.netty4.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by jbrisbin on 3/26/15.
 */
public class TcpConnectionEventListenerChannelHandler extends ChannelDuplexHandler {

	private static final AtomicLongFieldUpdater<TcpConnectionEventListenerChannelHandler> WRITTEN_UPD
			= AtomicLongFieldUpdater.newUpdater(TcpConnectionEventListenerChannelHandler.class, "writtenSinceLastFlush");

	private final TcpConnection connection;

	private volatile long writtenSinceLastFlush = 0L;

	private WriteCompleteListener writeCompleteListener;

	public TcpConnectionEventListenerChannelHandler(TcpConnection connection) {
		this.connection = connection;
	}

	public WriteCompleteListener getWriteCompleteListener() {
		return writeCompleteListener;
	}

	public void setWriteCompleteListener(WriteCompleteListener writeCompleteListener) {
		this.writeCompleteListener = writeCompleteListener;
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

}
