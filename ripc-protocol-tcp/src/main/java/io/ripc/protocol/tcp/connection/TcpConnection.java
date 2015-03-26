package io.ripc.protocol.tcp.connection;

import org.reactivestreams.Publisher;

/**
 * A {@code Connection} provides a reader for inbound data and a writer for outbound.
 */
public interface TcpConnection {

	Publisher<?> reader();

	TcpConnection writer(Publisher<?> writer);

	TcpConnection addListener(TcpConnectionEventListener listener);

}
