package io.ripc.composition.reactor.tcp;

import io.ripc.composition.reactor.tcp.connection.ReactorTcpConnection;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.transport.netty4.tcp.server.NettyTcpServer;
import org.reactivestreams.Subscriber;
import reactor.rx.Stream;
import reactor.rx.broadcast.Broadcaster;

/**
 * An implementation of a TCP server that emits new connections as they are created.
 */
public class ReactorTcpServer<R, W> extends Stream<ReactorTcpConnection<R, W>> {

	private final Broadcaster<ReactorTcpConnection<R, W>> connections = Broadcaster.create();

	private TcpServer<ReactorTcpConnection<R, W>> server;

	public ReactorTcpServer(int port, Class<R> readType, Class<W> writeType) {
		this.server = NettyTcpServer.listen(port)
		                            .intercept(conn -> new ReactorTcpConnection<>(conn, readType, writeType))
		                            .handler(connections::onNext);
	}

	public static <R, W> ReactorTcpServer<R, W> listen(int port, Class<R> readType, Class<W> writeType) {
		return new ReactorTcpServer<>(port, readType, writeType);
	}

	public void shutdown() {
		server.shutdown();
	}

	@Override
	public void subscribe(Subscriber<? super ReactorTcpConnection<R, W>> s) {
		server.start();
		connections.subscribe(s);
	}

}
