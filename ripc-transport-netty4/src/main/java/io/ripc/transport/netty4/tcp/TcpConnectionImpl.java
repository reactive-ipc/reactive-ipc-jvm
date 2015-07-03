package io.ripc.transport.netty4.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.transport.netty4.tcp.ChannelToConnectionBridge.ConnectionInputSubscriberEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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
                nettyChannel.write(data).addListener(new FutureToSubscriberBridge(s));
            }
        };
    }

    @Override
    public void subscribe(Subscriber<? super R> s) {
        nettyChannel.pipeline().fireUserEventTriggered(new ConnectionInputSubscriberEvent<>(s));
    }

    private static final Subscription IGNORE_SUBSCRIPTION = new Subscription() {
        @Override
        public void request(long n) {
            //IGNORE
        }

        @Override
        public void cancel() {
            //IGNORE
        }
    };

    private static class FutureToSubscriberBridge implements ChannelFutureListener {

        private final Subscriber<? super Void> subscriber;

        public FutureToSubscriberBridge(Subscriber<? super Void> subscriber) {
            this.subscriber = subscriber;
            subscriber.onSubscribe(IGNORE_SUBSCRIPTION);
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

}
