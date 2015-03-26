package io.ripc.protocol.tcp.connection;

/**
 * A {@code ConnectionHandler} is responsible for composing a Reactive Streams pipeline(s) when a new connection is
 * received by the server. Implementations will compose an appropriate pipeline based on capabilities and server
 * configuration.
 */
public interface TcpConnectionHandler {

	void handle(TcpConnection connection);

}
