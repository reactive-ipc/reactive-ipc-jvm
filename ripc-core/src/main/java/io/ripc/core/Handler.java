package io.ripc.core;

/**
 * A {@code Handler} takes an arbitrary object as input and "handles" it.
 */
public interface Handler<T> {
	/**
	 * Handle the given object (do something useful with it).
	 * @param obj
	 */
	void handle(T obj);
}
