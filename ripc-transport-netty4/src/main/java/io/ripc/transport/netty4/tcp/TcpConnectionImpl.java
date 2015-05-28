package io.ripc.transport.netty4.tcp;

import io.netty.channel.Channel;
import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.transport.netty4.SubscriberListener;
import io.ripc.transport.netty4.tcp.ChannelToConnectionBridge.ConnectionInputSubscriberEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class TcpConnectionImpl<R, W> implements TcpConnection<R, W> {

	private final Channel nettyChannel;

	public TcpConnectionImpl(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
	}

	@Override
	public Publisher<Void> write(final Publisher<? extends W> data) {
		return new Publisher<Void>() {
			@Override
			public void subscribe(Subscriber<? super Void> s) {
				nettyChannel.write(data)
				            .addListener(new SubscriberListener(s));
			}
		};
	}

	@Override
	public void subscribe(Subscriber<? super R> s) {
		nettyChannel.pipeline()
		            .fireUserEventTriggered(new ConnectionInputSubscriberEvent<>(s));
	}

}
