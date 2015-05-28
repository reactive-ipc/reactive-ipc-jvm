package io.ripc.protocol.tcp;

import org.reactivestreams.Publisher;

/**
 * An abstraction for a TCP connection.
 *
 * @param <R> The type of objects read from this connection.
 * @param <W> The type of objects written to this connection.
 */
public interface TcpConnection<R, W> extends Publisher<R> {

    /**
     * Writes the passed stream of {@code data} and returns the result as a {@link Publisher}. All items emitted by
     * this stream are flushed on completion of the stream.
     *
     * @param data Data stream to write.
     *
     * @return Result of write.
     */
	Publisher<Void> write(Publisher<? extends W> data);

}
