package io.ripc.rx.protocol.tcp;

import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.protocol.tcp.TcpHandler;
import io.ripc.protocol.tcp.TcpServer;
import org.reactivestreams.Publisher;
import rx.RxReactiveStreams;

public final class RxTcpServer<R, W> {

    private final TcpServer<R, W> transport;

    private RxTcpServer(final TcpServer<R, W> transport) {
        this.transport = transport;
    }

    public RxTcpServer<R, W> start(final RxTcpHandler<R, W> handler) {

        transport.start(new TcpHandler<R, W>() {
            @Override
            public Publisher<Void> handle(TcpConnection<R, W> connection) {
                return RxReactiveStreams.toPublisher(handler.handle(RxConnection.create(connection)));
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

    public int getPort() {
        return transport.getPort();
    }

    public static <R, W> RxTcpServer<R, W> create(TcpServer<R, W> transport) {
        return new RxTcpServer<>(transport);
    }
}
