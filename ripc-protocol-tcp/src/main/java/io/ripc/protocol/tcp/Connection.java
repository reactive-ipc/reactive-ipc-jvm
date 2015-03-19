package io.ripc.protocol.tcp;

import org.reactivestreams.Publisher;

/**
 * An abstraction for a TCP connection.
 *
 * @param <R> The type of objects read from this connection.
 * @param <W> The type of objects written to this connection.
 */
public interface Connection<R, W> extends Publisher<R> {

    /**
     * Writes the passed stream of {@code data} and returns the result as a {@link Publisher}. All items emitted by
     * this stream are flushed on completion of the stream.
     *
     * @param data Data stream to write.
     *
     * @return Result of write.
     */
	Publisher<Void> write(Publisher<W> data);

    /**
     * Writes the passed stream of {@code data} and returns the result as a {@link Publisher}. All written items are
     * flushed whenever the passed {@code flushSelector} returns {@code true}
     *
     * @param data Data stream to write.
     * @param flushSelector Selector that is invoked after every emitted item is written. If this selector returns
     * {@code true} then all items written till now are flushed.
     *
     * @return Result of write.
     */
    Publisher<Void> write(Publisher<W> data, FlushSelector<W> flushSelector);

    /**
     * A function that is used for determining when a flush has to be invoked on the underlying channel.
     *
     * @param <W> Type of items emitted by the stream using this selector.
     */
    interface FlushSelector<W> {

        /**
         * Selects whether flush should be invoked on the channel.
         *
         * @param count The index of this item. Starts with 1.
         * @param lastWrittenItem Item which was last written before calling this selector.
         *
         * @return {@code true} if flush should be invoked.
         */
        boolean select(long count, W lastWrittenItem);

    }
}
