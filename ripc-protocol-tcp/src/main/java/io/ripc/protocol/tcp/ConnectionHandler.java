package io.ripc.protocol.tcp;

/**
 * A {@code ConnectionHandler} is responsible for composing a Reactive Stream pipeline(s) when a new connection is
 * received by the server.
 */
public interface ConnectionHandler<B> {

	/**
	 * Implementations will compose an appropriate pipeline based on capabilities and server configuration.
	 *
	 * @param connection connection around which to compose the pipeline
	 */
	void handle(Connection<B> connection);

}
