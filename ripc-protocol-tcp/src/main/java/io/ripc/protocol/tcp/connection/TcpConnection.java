package io.ripc.protocol.tcp.connection;

import io.ripc.core.EventListener;
import org.reactivestreams.Publisher;

/**
 * A {@code Connection} provides a reader for inbound data and a writer for outbound.
 */
public interface TcpConnection {

	/**
	 * Receive the {@link org.reactivestreams.Publisher} which will be producing inbound data. This is intended to be
	 * composed into a more sophisticated processing pipeline by a higher layer of composition helpers.
	 *
	 * @return {@code this}
	 */
	Publisher<Object> reader();

	/**
	 * Set the write {@link org.reactivestreams.Publisher} that will be used to send outbound data to the peer. Can only
	 * be invoked once.
	 *
	 * @param writer the {@link org.reactivestreams.Publisher} which will produce outbound data
	 * @return {@code this}
	 */
	TcpConnection writer(Publisher<Object> writer);

	/**
	 * Add a type of {@link io.ripc.core.EventListener} to the connection which will respond to the events for which that
	 * specific listener type is responsible. What's available varies by protocol and server implementation.
	 *
	 * @param listener
	 * @return {@code this}
	 */
	TcpConnection addListener(EventListener listener);

}
