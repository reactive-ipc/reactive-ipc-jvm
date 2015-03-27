package io.ripc.composition.reactor.tcp.connection;

import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpConnection<T> {

	private final Broadcaster<T> writeComplete = Broadcaster.create();

	private final TcpConnection connection;
	private final Stream<T>     in;

	@SuppressWarnings("unchecked")
	public ReactorTcpConnection(TcpConnection connection, Class<T> type) {
		this.connection = connection;

		this.in = Streams.wrap(connection.reader()).map(type::cast);
		this.connection.addListener(new WriteCompleteListener() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean writeComplete(TcpConnection connection, long count, Object msg) {
				writeComplete.onNext(type.cast(msg));
				return false;
			}
		});
	}

	public Stream<T> in() {
		return in;
	}

	public Stream<T> out(Stream<T> out) {
		connection.writer(out.map(Object.class::cast));
		return writeComplete;
	}

}
