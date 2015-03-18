package io.ripc.protocol.tcp;

import io.ripc.core.io.Buffer;
import org.reactivestreams.Publisher;

/**
 * A {@code Connection} is a Reactive Streams {@link org.reactivestreams.Publisher} that provides subscribers with
 * inbound data and exposes the {@link #write(org.reactivestreams.Publisher)} method for sending outbound data.
 */
public interface Connection<B> extends Publisher<Buffer<B>> {

	/**
	 * Send outbound data using the Reactive Streams {@code Publisher} contract.
	 *
	 * @param data publisher of outbound data
	 */
	void write(Publisher<Buffer<B>> data);

}
