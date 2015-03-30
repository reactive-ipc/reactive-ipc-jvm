package io.ripc.core;

/**
 * An {@code Interceptor} takes one object, intercepts it, and transforms it into something else.
 */
public interface Interceptor<T, V> {

	/**
	 * Transform T to V.
	 *
	 * @param obj
	 * @return
	 */
	V intercept(T obj);

}
