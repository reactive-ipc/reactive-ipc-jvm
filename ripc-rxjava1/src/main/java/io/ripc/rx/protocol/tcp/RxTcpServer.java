package io.ripc.rx.protocol.tcp;

import io.ripc.protocol.tcp.Connection;
import io.ripc.protocol.tcp.TcpHandler;
import io.ripc.protocol.tcp.TcpInterceptor;
import io.ripc.protocol.tcp.TcpServer;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;

public final class RxTcpServer<R, W> {

    private final TcpServer<R, W> transport;

    private RxTcpServer(final TcpServer<R, W> transport) {
        this.transport = transport;
    }

    public <RR, WW> RxTcpServer<RR, WW> intercept(final RxTcpInterceptor<R, W, RR, WW> interceptor) {
        return new RxTcpServer<>(transport.intercept(new TcpInterceptor<R, W, RR, WW>() {
            @Override
            public TcpHandler<RR, WW> intercept(final TcpHandler<R, W> rsHandler) {
                /*Create once, not per connection*/
                final RxTcpHandler<RR, WW> rxHandler = interceptor.intercept(new RxTcpHandler<R, W>() {
                    @Override
                    public Observable<Void> handle(RxConnection<R, W> connection) {
                        return RxReactiveStreams.toObservable(rsHandler.handle(connection.getDelegate()));
                    }
                });

                return new TcpHandler<RR, WW>() {
                    @Override
                    public Publisher<Void> handle(Connection<RR, WW> connection) {
                        return RxReactiveStreams.toPublisher(rxHandler.handle(RxConnection.create(connection)));
                    }
                };
            }
        }));
    }

    public RxTcpServer<R, W> start(final RxTcpHandler<R, W> handler) {

        transport.start(new TcpHandler<R, W>() {
            @Override
            public Publisher<Void> handle(Connection<R, W> connection) {
                return RxReactiveStreams.toPublisher(handler.handle(new RxConnection<>(connection)));
            }
        });

        return this;
    }

    public void startAndAwait(RxTcpHandler<R, W> handler) {
        start(handler);
        transport.awaitShutdown();
    }

    public final boolean shutdown() {
        return transport.shutdown();
    }

    public void awaitShutdown() {
        transport.awaitShutdown();
    }

    public static <R, W> RxTcpServer<R, W> create(TcpServer<R, W> transport) {
        return new RxTcpServer<>(transport);
    }
}
