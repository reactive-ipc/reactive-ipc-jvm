package io.ripc.protocol.tcp.connection.listener;

import io.ripc.core.EventListener;
import io.ripc.protocol.tcp.connection.TcpConnection;

/**
 * An {@code EventListener} implementation that is used to receive notifications that an IO channel has completed the
 * read of all available data.
 */
public interface ReadCompleteListener extends EventListener {

	/**
	 * Invoked when all data has been read from the given {@link io.ripc.protocol.tcp.connection.TcpConnection}.
	 * Returning
	 * {@code true} here means "I'm finished with the connection you may close it now" and returning {@code false} means
	 * "leave the connection open".
	 *
	 * @param connection the connection on which reads are complete
	 * @return {@code true} to close the connection, {@code false} to leave it open
	 */
	boolean readComplete(TcpConnection connection);

}
