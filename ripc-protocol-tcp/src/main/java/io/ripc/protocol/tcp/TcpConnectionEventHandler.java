package io.ripc.protocol.tcp;

/**
 * Created by jbrisbin on 3/26/15.
 */
public interface TcpConnectionEventHandler {

	void onOpen(TcpConnection connection);

	void onClose(TcpConnection connection);

	void onAbort(TcpConnection connection);

	void onError(TcpConnection connection, Throwable cause);

	void onReadable(TcpConnection connection);

	void onWritable(TcpConnection connection);

}
