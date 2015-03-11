package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.ripc.core.io.Buffer;
import io.ripc.protocol.tcp.Connection;
import io.ripc.transport.netty4.tcp.ChannelInboundHandlerSubscription;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NettyTcpServerConnection implements Connection<ByteBuf> {

	private final List<Subscriber<? super Buffer<ByteBuf>>> subscribers = new ArrayList<>();

	private final Channel channel;

	public NettyTcpServerConnection(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void write(Publisher<Buffer<ByteBuf>> data) {

	}

	@Override
	public void subscribe(Subscriber<? super Buffer<ByteBuf>> s) {
		ChannelInboundHandlerSubscription sub = new ChannelInboundHandlerSubscription(channel, s);
		synchronized (this) {
			subscribers.add(s);
		}
		s.onSubscribe(sub);
		channel.pipeline()
		       .addLast(new LoggingHandler(LogLevel.DEBUG), sub);
	}

	@Override
	public String toString() {
		return "NettyTcpServerConnection{" +
				"channel=" + channel +
				'}';
	}

}
