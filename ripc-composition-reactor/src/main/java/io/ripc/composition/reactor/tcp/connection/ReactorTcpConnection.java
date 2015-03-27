package io.ripc.composition.reactor.tcp.connection;

import io.ripc.protocol.tcp.connection.TcpConnection;
import reactor.rx.Stream;
import reactor.rx.Streams;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpConnection<T> {

	private final TcpConnection connection;
	private final Stream<T>     in;

	@SuppressWarnings("unchecked")
	public ReactorTcpConnection(TcpConnection connection, Class<T> type) {
		this.connection = connection;
		this.in = Streams.wrap(connection.reader()).map(type::cast);
	}

	public Stream<T> in() {
		return in;
	}

	public ReactorTcpConnection<T> out(Stream<T> out) {
		connection.writer(out.map(Object.class::cast));
		return this;
	}

}
