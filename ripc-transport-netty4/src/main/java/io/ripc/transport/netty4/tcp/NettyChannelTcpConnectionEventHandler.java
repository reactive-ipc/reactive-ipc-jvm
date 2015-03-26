package io.ripc.transport.netty4.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.ripc.protocol.tcp.CompleteEventHandler;
import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.protocol.tcp.TcpConnectionEventHandler;

/**
 * Created by jbrisbin on 3/26/15.
 */
public class NettyChannelTcpConnectionEventHandler extends ChannelDuplexHandler {

	private final TcpConnectionEventHandler eventHandler;
	private final TcpConnection             connection;
	private final CompleteEventHandler      completeEventHandler;

	public NettyChannelTcpConnectionEventHandler(TcpConnectionEventHandler eventHandler, TcpConnection connection) {
		this.eventHandler = eventHandler;
		this.connection = connection;
		this.completeEventHandler = eventHandler instanceof CompleteEventHandler
		                            ? (CompleteEventHandler) eventHandler
		                            : null;
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
		eventHandler.onAbort(connection);
		super.disconnect(ctx, future);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
		eventHandler.onClose(connection);
		super.close(ctx, future);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (null != completeEventHandler) {
			promise.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess() && completeEventHandler.onWriteComplete(connection, msg)) {
						ctx.flush();
					}
				}
			});
		}
		super.write(ctx, msg, promise);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		eventHandler.onOpen(connection);
		eventHandler.onReadable(connection);
		super.channelActive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		eventHandler.onError(connection, cause);
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		eventHandler.onWritable(connection);
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		if (null != completeEventHandler) {
			if (!completeEventHandler.onReadComplete(connection)) {
				ctx.close();
			}
		}
		super.channelReadComplete(ctx);
	}

}
