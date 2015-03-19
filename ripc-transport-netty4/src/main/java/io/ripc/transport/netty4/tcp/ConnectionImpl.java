package io.ripc.transport.netty4.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.ripc.protocol.tcp.Connection;
import io.ripc.transport.netty4.tcp.ChannelToConnectionBridge.ConnectionInputSubscriberEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ConnectionImpl<R, W> implements Connection<R, W> {

    private final Channel nettyChannel;

    public ConnectionImpl(Channel nettyChannel) {
        this.nettyChannel = nettyChannel;
    }

    @Override
    public Publisher<Void> write(final Publisher<W> data) {
        return new Publisher<Void>() {
            @Override
            public void subscribe(Subscriber<? super Void> s) {
                bridgeFutureToSub(nettyChannel.write(data), s);
            }
        };
    }

    @Override
    public Publisher<Void> write(final Publisher<W> data, final FlushSelector<W> flushSelector) {
        return new Publisher<Void>() {
            @Override
            public void subscribe(Subscriber<? super Void> s) {

                final Publisher<W> flushAwareData = new Publisher<W>() {
                    @Override
                    public void subscribe(final Subscriber<? super W> subscriber) {
                        data.subscribe(new Subscriber<W>() {
                            private long itemCount;

                            @Override
                            public void onSubscribe(Subscription subscription) {
                                subscriber.onSubscribe(subscription);
                            }

                            @Override
                            public void onNext(W w) {
                                subscriber.onNext(w);
                                if (flushSelector.select(++itemCount, w)) {
                                    nettyChannel.flush();
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                subscriber.onError(t);
                            }

                            @Override
                            public void onComplete() {
                                subscriber.onComplete();
                            }
                        });
                    }
                };

                bridgeFutureToSub(nettyChannel.write(flushAwareData), s);
            }
        };
    }

    @Override
    public void subscribe(Subscriber<? super R> s) {
        nettyChannel.pipeline().fireUserEventTriggered(new ConnectionInputSubscriberEvent<>(s));
    }

    private void bridgeFutureToSub(ChannelFuture future, final Subscriber<? super Void> s) {
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    s.onComplete();
                } else {
                    s.onError(future.cause());
                }
            }
        });
    }
}
