package io.ripc.reactor.protocol.tcp;

import org.reactivestreams.Publisher;
import reactor.fn.Function;

public interface ReactorTcpHandler<R, W> extends Function<ReactorTcpConnection<R, W>, Publisher<Void>> {

}