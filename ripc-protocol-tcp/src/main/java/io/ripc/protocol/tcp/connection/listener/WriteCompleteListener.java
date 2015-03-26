package io.ripc.protocol.tcp.connection.listener;

import io.ripc.core.EventListener;
import io.ripc.protocol.tcp.connection.TcpConnection;

/**
 * An {@code EventListener} implementation that is used to receive notifications that writes have been successfully
 * completed on the underlying IO channel.
 */
public interface WriteCompleteListener extends EventListener {

	/**
	 * Invoked every time a write to the IO channel is complete. Returning {@code false} here means "don't do any
	 * flushing" and returning {@code true} means "flush the underlying IO channel".
	 *
	 * @param connection the connection on which the write was made
	 * @param count      the number of items that have been written since the last flush
	 * @param msg        the last value written
	 * @return {@code true} to perform a flush of the channel, {@code false} otherwise
	 */
	boolean writeComplete(TcpConnection connection, long count, Object msg);

}
