package io.ripc.core;

/**
 * Simple functional interface for applying transformations to objects.
 */
@FunctionalInterface
public interface Function<T, V> {
	V apply(T obj);
}
