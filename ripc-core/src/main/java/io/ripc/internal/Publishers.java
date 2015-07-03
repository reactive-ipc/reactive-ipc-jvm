package io.ripc.internal;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Temporary utility class for creating and transforming {@link Publisher}s.
 */
public class Publishers {

    public static <T> Publisher<T> just(final T... values) {
        return new Publisher<T>() {
            @Override
            public void subscribe(final Subscriber<? super T> s) {
                s.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        for (T value : values) {
                            s.onNext(value);
                        }
                        s.onComplete();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };
    }

    private static final Subscription ERROR_SUB = new Subscription() {
        @Override
        public void request(long n) {
            //ignore
        }

        @Override
        public void cancel() {
            //ignore
        }
    };

    public static <T> Publisher<T> error(final Throwable t) {
        return new Publisher<T>() {
            @Override
            public void subscribe(final Subscriber<? super T> s) {
                s.onSubscribe(ERROR_SUB);
                s.onError(t);
            }
        };
    }
}
