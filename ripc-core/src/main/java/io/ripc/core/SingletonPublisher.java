package io.ripc.core;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@code SingletonPublisher} provides a single value only once and then calls {@code onComplete}. If the value is
 * {@code null}, then the {@link org.reactivestreams.Subscriber#onNext(Object)} is not called but {@link
 * org.reactivestreams.Subscriber#onComplete()} is.
 */
public class SingletonPublisher<T> implements Publisher<T>, DemandCalculator {

	private final AtomicBoolean requested = new AtomicBoolean(false);

	private final T value;

	public SingletonPublisher(T value) {
		this.value = value;
	}

	@Override
	public long calculateDemand(long pending) {
		return (requested.get() ? -1 : 1);
	}

	@Override
	public void subscribe(final Subscriber<? super T> subscriber) {
		subscriber.onSubscribe(new Subscription() {
			@Override
			public void request(long n) {
				if (!Specification.spec_3_9_verifyPositiveDemand(n, subscriber)) {
					return;
				}
				if (requested.compareAndSet(false, true)) {
					if (null != value) {
						subscriber.onNext(value);
					}
					subscriber.onComplete();
				}
			}

			@Override
			public void cancel() {
				requested.set(true);
			}
		});
	}

}
