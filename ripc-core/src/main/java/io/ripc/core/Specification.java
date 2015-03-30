package io.ripc.core;

import org.reactivestreams.Subscriber;

/**
 * Helper class to encapsulate various checks required by the Reactive Streams Specification.
 */
public abstract class Specification {

	protected Specification() {
	}

	public static <T> boolean spec_3_9_verifyPositiveDemand(long demand, Subscriber<T> subscriber) {
		if (demand > 0) {
			return true;
		}
		subscriber.onError(new IllegalArgumentException("Spec 3.9: Request signals must be a positive number."));
		return false;
	}

}
