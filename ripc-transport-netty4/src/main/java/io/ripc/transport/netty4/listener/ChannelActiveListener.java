package io.ripc.transport.netty4.listener;

import io.netty.channel.ChannelHandlerContext;
import io.ripc.core.EventListener;

/**
 * Created by jbrisbin on 3/30/15.
 */
public interface ChannelActiveListener extends EventListener {
	void channelActive(ChannelHandlerContext ctx);
}
