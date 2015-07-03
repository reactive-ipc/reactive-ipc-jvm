package io.ripc.test;

import io.ripc.test.internal.PublisherFactory;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.List;

/**
 * Temporary utility class for creating and transforming {@link Publisher}s.
 */
public class Publishers {

    public static <T> Publisher<T> just(final T... values) {
        final List<T> list = Arrays.asList(values);
        return PublisherFactory.forEach(
            sub -> {
                if (sub.context().hasNext()) {
                    sub.onNext(sub.context().next());
                } else {
                    sub.onComplete();
                }
            },
            sub -> list.iterator()
        );
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
