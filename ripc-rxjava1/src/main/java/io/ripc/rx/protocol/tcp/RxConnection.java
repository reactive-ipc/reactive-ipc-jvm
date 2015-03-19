package io.ripc.rx.protocol.tcp;

import io.ripc.protocol.tcp.Connection;
import io.ripc.protocol.tcp.Connection.FlushSelector;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Subscriber;

import static rx.RxReactiveStreams.*;

/**
 * An adapter for {@link Connection} representated as an {@link Observable}
 *
 * @param <R> The type of objects read from this connection.
 * @param <W> The type of objects written to this connection.
 */
public class RxConnection<R, W> extends Observable<R> {

    private final Connection<R, W> delegate;

    protected RxConnection(final Connection<R, W> delegate) {
        super(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> subscriber) {
                RxReactiveStreams.subscribe(delegate, subscriber);
            }
        });
        this.delegate = delegate;
    }

    /**
     * Writes the passed stream of {@code data} and returns the result as an {@link Observable}. All items emitted by
     * this stream are flushed on completion of the stream.
     *
     * @param data Data stream to write.
     *
     * @return Result of write.
     */
    public Observable<Void> write(Observable<W> data) {
        return toObservable(delegate.write(toPublisher(data)));
    }

    /**
     * Writes the passed stream of {@code data} and returns the result as an {@link Observable}. All written items are
     * flushed whenever the passed {@code flushSelector} returns {@code true}
     *
     * @param data Data stream to write.
     * @param flushSelector Selector that is invoked after every emitted item is written. If this selector returns
     * {@code true} then all items written till now are flushed.
     *
     * @return Result of write.
     */
    public Observable<Void> write(Observable<W> data, FlushSelector<W> flushSelector) {
        return toObservable(delegate.write(toPublisher(data), flushSelector));
    }

    Connection<R, W> getDelegate() {
        return delegate;
    }

    public static <R, W> RxConnection<R, W> create(Connection<R, W> delegate) {
        return new RxConnection<>(delegate);
    }
}
