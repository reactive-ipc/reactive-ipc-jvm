package io.ripc.core;

/**
 * Simple functional interface for accepting objects via callback.
 */
@FunctionalInterface
public interface Consumer<T> {

	void accept(T obj);

}
