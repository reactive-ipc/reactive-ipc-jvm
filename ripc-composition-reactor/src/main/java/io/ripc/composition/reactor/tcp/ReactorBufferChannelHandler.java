package io.ripc.composition.reactor.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import reactor.io.buffer.Buffer;

/**
 * Created by jbrisbin on 3/30/15.
 */
@ChannelHandler.Sharable
public class ReactorBufferChannelHandler extends ChannelDuplexHandler {

	public static final ChannelDuplexHandler INSTANCE = new ReactorBufferChannelHandler();

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (Buffer.class.isAssignableFrom(msg.getClass())) {
			Buffer reactorBuf = (Buffer) msg;
			ByteBuf nettyBuf = ctx.alloc().buffer(reactorBuf.capacity());
			nettyBuf.writeBytes(reactorBuf.asBytes());
			msg = nettyBuf;
		}
		super.write(ctx, msg, promise);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (ByteBuf.class.isAssignableFrom(msg.getClass())) {
			msg = new Buffer(((ByteBuf) msg).nioBuffer());
		}
		super.channelRead(ctx, msg);
	}

}
