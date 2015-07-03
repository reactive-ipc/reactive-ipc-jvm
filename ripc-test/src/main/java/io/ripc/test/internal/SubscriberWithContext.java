/*
 * Copyright (c) 2011-2015 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ripc.test.internal;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Original {@see reactor.core.reactivestreams.PublisherFactory} from {@see http://projectreactor.io}.
 *
 * A {@link Subscriber} with a typed stateful context. Some error isolation is also provided
 * (onSubscribe, onNext and onComplete error is forwarded to onError).
 *
 * @author Stephane Maldini
 */
public class SubscriberWithContext<T, C> implements Subscriber<T> {

	private volatile       int                                              terminated       = 0;
	protected static final AtomicIntegerFieldUpdater<SubscriberWithContext> TERMINAL_UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(SubscriberWithContext.class, "terminated");


	protected final C                     context;
	protected final Subscriber<? super T> subscriber;

	/**
	 * Attach a given arbitrary context (stateful information) to a {@link Subscriber}, all Subscriber methods
	 * will delegate properly.
	 *
	 * @param subscriber the delegate subscriber to invoke on signal
	 * @param context    the contextual state of any type to bind for later use
	 * @param <T>        Type of data sequence
	 * @param <C>        Type of attached stateful context
	 * @return a new Susbscriber with context information
	 */
	public static <T, C> SubscriberWithContext<T, C> create(Subscriber<? super T> subscriber, C context) {
		return new SubscriberWithContext<>(context, subscriber);
	}

	protected SubscriberWithContext(C context, Subscriber<? super T> subscriber) {
		this.context = context;
		this.subscriber = subscriber;
	}

	/**
	 * The stateful context C
	 *
	 * @return the bound context
	 */
	public C context() {
		return context;
	}

	@Override
	public void onSubscribe(Subscription s) {
		try {
			subscriber.onSubscribe(s);
		} catch (Throwable throwable) {
			subscriber.onError(throwable);
		}
	}

	@Override
	public void onNext(T t) {
		try {
			subscriber.onNext(t);
		} catch (Throwable throwable) {
			subscriber.onError(throwable);
		}
	}

	@Override
	public void onError(Throwable t) {
		if (TERMINAL_UPDATER.compareAndSet(this, 0, 1)) {
			subscriber.onError(t);
		}
	}

	@Override
	public void onComplete() {
		try {
			if (TERMINAL_UPDATER.compareAndSet(this, 0, 1)) {
				subscriber.onComplete();
			}
		} catch (Throwable throwable) {
			subscriber.onError(throwable);
		}
	}

	public boolean isCancelled(){
		return terminated == 1;
	}
}
