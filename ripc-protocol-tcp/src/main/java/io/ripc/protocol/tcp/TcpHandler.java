package io.ripc.protocol.tcp;

import org.reactivestreams.Publisher;

public interface TcpHandler<R, W> {

    Publisher<Void> handle(TcpConnection<R, W> connection);
}
