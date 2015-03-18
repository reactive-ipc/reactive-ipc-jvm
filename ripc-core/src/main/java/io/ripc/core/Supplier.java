package io.ripc.core;

/**
 * A simple functional interface to provide instances of a given object.
 */
@FunctionalInterface
public interface Supplier<T> {
	T get();
}
