package io.ripc.rx.protocol.tcp;

import io.ripc.protocol.tcp.TcpConnection;
import rx.Observable;
import rx.Subscriber;
import rx.internal.reactivestreams.SubscriberAdapter;

import static rx.RxReactiveStreams.*;

/**
 * An adapter for {@link io.ripc.protocol.tcp.TcpConnection} representated as an {@link Observable}
 *
 * @param <R> The type of objects read from this connection.
 * @param <W> The type of objects written to this connection.
 */
public class RxConnection<R, W> extends Observable<R> {

    private final TcpConnection<R, W> delegate;

    protected RxConnection(final TcpConnection<R, W> delegate) {
        super(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> subscriber) {
                delegate.subscribe(new SubscriberAdapter<>(subscriber));
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

    public static <R, W> RxConnection<R, W> create(TcpConnection<R, W> delegate) {
        return new RxConnection<>(delegate);
    }
}
