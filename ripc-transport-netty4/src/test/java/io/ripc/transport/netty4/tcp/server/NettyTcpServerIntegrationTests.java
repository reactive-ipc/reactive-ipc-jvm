package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.ByteBuf;
import io.ripc.core.io.Buffer;
import io.ripc.protocol.tcp.Connection;
import io.ripc.protocol.tcp.ConnectionPublisher;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class NettyTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(NettyTcpServerIntegrationTests.class);

	@Test
	public void canStartNettyTcpServer() throws InterruptedException {
		ConnectionPublisher<ByteBuf> server = NettyTcpServer.listen(3000);
		CountDownLatch latch = new CountDownLatch(1);

		server.subscribe(new Subscriber<Connection<ByteBuf>>() {
			@Override
			public void onSubscribe(Subscription s) {
				s.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(Connection<ByteBuf> conn) {
				LOG.debug("new connection={}", conn);

				conn.subscribe(new Subscriber<Buffer<ByteBuf>>() {
					@Override
					public void onSubscribe(Subscription s) {
						s.request(Long.MAX_VALUE);
					}

					@Override
					public void onNext(Buffer<ByteBuf> buffer) {
						LOG.debug("data received: {}", buffer);
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {
						LOG.debug("connection closed");
						latch.countDown();
					}
				});
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onComplete() {
			}
		});

		while (!latch.await(1, TimeUnit.SECONDS)) {
			Thread.sleep(1000);
		}
	}

}
