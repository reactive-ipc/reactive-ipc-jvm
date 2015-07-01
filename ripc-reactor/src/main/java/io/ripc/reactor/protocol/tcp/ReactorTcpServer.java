package io.ripc.reactor.protocol.tcp;

import io.ripc.protocol.tcp.TcpConnection;
import io.ripc.protocol.tcp.TcpHandler;
import io.ripc.protocol.tcp.TcpServer;
import org.reactivestreams.Publisher;
import reactor.Environment;
import reactor.fn.Function;

/**
 * Created by jbrisbin on 5/28/15.
 */
public class ReactorTcpServer<R, W> {

    static {
        Environment.initializeIfEmpty().assignErrorJournal();
    }

    private final TcpServer<R, W> transport;

    ReactorTcpServer(TcpServer<R, W> transport) {
        this.transport = transport;
    }

    public ReactorTcpServer<R, W> start(final ReactorTcpHandler<R, W> handler) {

        transport.start(new TcpHandler<R, W>() {
            @Override
            public Publisher<Void> handle(TcpConnection<R, W> connection) {
                return handler.apply(new ReactorTcpConnection<>(connection));
            }
        });
        return this;
    }

    public ReactorTcpServer<R, W> startAndAwait(final ReactorTcpHandler<R, W> handler) {

        transport.startAndAwait(new TcpHandler<R, W>() {
            @Override
            public Publisher<Void> handle(TcpConnection<R, W> connection) {
                return handler.apply(new ReactorTcpConnection<>(connection));
            }
        });
        return this;
    }

    public boolean shutdown() {
        boolean b = transport.shutdown();
        transport.awaitShutdown();
        return b;
    }

    public int getPort() {
        return transport.getPort();
    }

    public static <R, W> ReactorTcpServer<R, W> create(TcpServer<R, W> transport) {
        return new ReactorTcpServer<>(transport);
    }

}
