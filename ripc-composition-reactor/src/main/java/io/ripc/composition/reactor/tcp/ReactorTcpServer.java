package io.ripc.composition.reactor.tcp;

import io.ripc.composition.reactor.tcp.connection.ReactorTcpConnection;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.transport.netty4.tcp.server.NettyTcpServer;
import org.reactivestreams.Subscriber;
import reactor.rx.Stream;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpServer<T> extends Stream<ReactorTcpConnection<T>> {

	private final Broadcaster<ReactorTcpConnection<T>> connections = Broadcaster.create();

	private TcpServer<ReactorTcpConnection<T>> server;

	public ReactorTcpServer(int port, Class<T> type) {
		this.server = NettyTcpServer.listen(port)
		                            .intercept(conn -> new ReactorTcpConnection<>(conn, type))
		                            .handler(connections::onNext);
		this.server.start();
	}

	public static <T> ReactorTcpServer<T> listen(int port, Class<T> type) {
		return new ReactorTcpServer<>(port, type);
	}


	public void shutdown() {
		server.shutdown();
	}

	@Override
	public void subscribe(Subscriber<? super ReactorTcpConnection<T>> s) {
		connections.subscribe(s);
	}

}
