package io.ripc.reactor.protocol.tcp;

import io.ripc.protocol.tcp.TcpConnection;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.rx.Stream;

/**
 * Created by jbrisbin on 5/28/15.
 */
public class ReactorTcpConnection<R, W> extends Stream<R> {

	private final TcpConnection<R, W> transport;

	public ReactorTcpConnection(TcpConnection<R, W> transport) {
		super();
		this.transport = transport;
	}

	public Publisher<Void> writeWith(Publisher<W> out) {
		return transport.write(out);
	}

	@Override
	public void subscribe(Subscriber<? super R> sub) {
		transport.subscribe(sub);
	}

}
