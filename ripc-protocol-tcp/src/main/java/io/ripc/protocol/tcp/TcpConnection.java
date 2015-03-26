package io.ripc.protocol.tcp;

import org.reactivestreams.Publisher;

/**
 * A {@code Connection} provides a reader for inbound data and a writer for outbound.
 */
public interface TcpConnection {

	TcpConnection eventHandler(TcpConnectionEventHandler eventHandler);

	Publisher<?> reader();

	TcpConnection writer(Publisher<?> sink);

}
