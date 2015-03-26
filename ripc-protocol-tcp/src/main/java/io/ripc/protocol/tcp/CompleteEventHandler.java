package io.ripc.protocol.tcp;

/**
 * Created by jbrisbin on 3/26/15.
 */
public interface CompleteEventHandler {

	boolean onReadComplete(TcpConnection connection);

	boolean onWriteComplete(TcpConnection connection, Object msg);

}
