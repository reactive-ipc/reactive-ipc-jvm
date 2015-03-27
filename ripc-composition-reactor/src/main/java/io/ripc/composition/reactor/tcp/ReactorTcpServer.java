package io.ripc.composition.reactor.tcp;

import io.ripc.composition.reactor.tcp.connection.ReactorTcpConnection;
import io.ripc.transport.netty4.tcp.server.NettyTcpServer;
import org.reactivestreams.Subscriber;
import reactor.rx.Stream;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpServer<T> extends Stream<ReactorTcpConnection<T>> {

	private final Broadcaster<ReactorTcpConnection<T>> connections = Broadcaster.create();

	private NettyTcpServer server;

	public ReactorTcpServer() {
	}

	public static <T> ReactorTcpServer<T> listen(int port, Class<T> type) {
		ReactorTcpServer<T> server = new ReactorTcpServer<>();

		server.server = NettyTcpServer.listen(port, connection -> {
			ReactorTcpConnection<T> conn = new ReactorTcpConnection<>(connection, type);
			server.connections.onNext(conn);
		});

		return server;
	}

	public void shutdown() {
		server.shutdown();
	}

	@Override
	public void subscribe(Subscriber<? super ReactorTcpConnection<T>> s) {
		connections.subscribe(s);
	}

}
