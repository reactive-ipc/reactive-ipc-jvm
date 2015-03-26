package io.ripc.protocol.tcp.connection.listener;

import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.TcpConnectionEventListener;

/**
 * Created by jbrisbin on 3/26/15.
 */
public interface WriteCompleteListener extends TcpConnectionEventListener {

	boolean writeComplete(TcpConnection connection, long count, Object msg);

}
