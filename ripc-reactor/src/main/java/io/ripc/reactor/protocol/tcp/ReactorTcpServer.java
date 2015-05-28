package io.ripc.reactor.protocol.tcp;

import io.ripc.protocol.tcp.TcpServer;
import org.reactivestreams.Publisher;
import reactor.Environment;
import reactor.fn.Function;

/**
 * Created by jbrisbin on 5/28/15.
 */
public class ReactorTcpServer<R, W> {

	static {
		Environment.initializeIfEmpty()
		           .assignErrorJournal();
	}

	private final TcpServer<R, W> transport;

	ReactorTcpServer(TcpServer<R, W> transport) {
		this.transport = transport;
	}

	public ReactorTcpServer<R, W> start(Function<ReactorTcpConnection<R, W>, Publisher<Void>> handler) {
		transport.startAndAwait(conn -> handler.apply(new ReactorTcpConnection<>(conn)));
		return this;
	}

	public boolean shutdown() {
		boolean b = transport.shutdown();
		transport.awaitShutdown();
		return b;
	}

	public static <R, W> ReactorTcpServer<R, W> create(TcpServer<R, W> transport) {
		return new ReactorTcpServer<>(transport);
	}

}
