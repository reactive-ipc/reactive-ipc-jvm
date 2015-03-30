package io.ripc.composition.reactor.tcp.connection;

import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpConnection<R, W> {

	private final Broadcaster<W> writeComplete = Broadcaster.create();

	private final TcpConnection connection;
	private final Stream<R>     in;

	@SuppressWarnings("unchecked")
	public ReactorTcpConnection(TcpConnection connection, Class<R> readType, Class<W> writeType) {
		this.connection = connection;

		this.in = Streams.wrap(connection.reader()).map(readType::cast);
		this.connection.addListener(new WriteCompleteListener() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean writeComplete(TcpConnection connection, long count, Object msg) {
				writeComplete.onNext(writeType.cast(msg));
				return false;
			}
		});
	}

	public Stream<R> in() {
		return in;
	}

	public Stream<W> out(Stream<W> out) {
		connection.writer(out.observeComplete(v -> writeComplete.onComplete())
		                     .when(Throwable.class, writeComplete::onError)
		                     .map(Object.class::cast));
		return writeComplete;
	}

}
