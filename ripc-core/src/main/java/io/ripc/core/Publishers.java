package io.ripc.core;

import org.reactivestreams.Publisher;

/**
 * Created by jbrisbin on 3/26/15.
 */
public final class Publishers {

	private Publishers() {
	}

	public static <T> Publisher<?> just(final T obj) {
		return new SingletonPublisher<>(obj);
	}

}
