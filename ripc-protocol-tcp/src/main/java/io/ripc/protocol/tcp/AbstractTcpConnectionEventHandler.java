package io.ripc.protocol.tcp;

/**
 * Created by jbrisbin on 3/26/15.
 */
public abstract class AbstractTcpConnectionEventHandler implements TcpConnectionEventHandler, CompleteEventHandler {

	@Override
	public void onOpen(TcpConnection connection) {
		// NO-OP
	}

	@Override
	public void onClose(TcpConnection connection) {
		// NO-OP
	}

	@Override
	public void onAbort(TcpConnection connection) {
		// NO-OP
	}

	@Override
	public void onError(TcpConnection connection, Throwable cause) {
		// NO-OP
	}

	@Override
	public void onReadable(TcpConnection connection) {
		// NO-OP
	}

	@Override
	public void onWritable(TcpConnection connection) {
		// NO-OP
	}

	@Override
	public boolean onReadComplete(TcpConnection connection) {
		return true;
	}

	@Override
	public boolean onWriteComplete(TcpConnection connection, Object msg) {
		return false;
	}

}
