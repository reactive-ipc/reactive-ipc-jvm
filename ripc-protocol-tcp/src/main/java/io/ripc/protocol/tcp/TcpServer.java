package io.ripc.protocol.tcp;

import io.ripc.core.Handler;
import io.ripc.core.Interceptor;

/**
 * A {@code TcpServer} is a comopnent that listens for incoming TCP connections.
 */
public interface TcpServer<C> {

	/**
	 * Transform the type of connection we're using for this server into something else.
	 *
	 * @param interceptor
	 * @param <NEWC>
	 * @return a new {@code TcpServer} of a different type
	 */
	<NEWC> TcpServer<NEWC> intercept(Interceptor<C, NEWC> interceptor);

	/**
	 * Set the handler that will be used to handle new connections.
	 *
	 * @param handler
	 * @return {@code this}
	 */
	TcpServer<C> handler(Handler<C> handler);

	/**
	 * Start this server and bind to the socket.
	 */
	void start();

	/**
	 * Gracefully shutdown any resources allocated by this server.
	 */
	void shutdown();

}
