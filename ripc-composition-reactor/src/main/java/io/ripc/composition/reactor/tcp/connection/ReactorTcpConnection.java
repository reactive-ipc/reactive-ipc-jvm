package io.ripc.composition.reactor.tcp.connection;

import io.ripc.protocol.tcp.connection.TcpConnection;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpConnection<T> {

	private final Stream<T>      in;
	private final Broadcaster<T> out;

	@SuppressWarnings("unchecked")
	public ReactorTcpConnection(TcpConnection connection, Class<T> type) {
		this.in = Streams.wrap(connection.reader()).map(type::cast);
		this.out = Broadcaster.create();
		connection.writer(out.map(Object.class::cast));
	}

	public Stream<T> in() {
		return in;
	}

	public Broadcaster<T> out() {
		return out;
	}

}
