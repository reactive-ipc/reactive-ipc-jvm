package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.Unpooled;
import io.ripc.core.Publishers;
import io.ripc.protocol.tcp.connection.listener.ReadCompleteListener;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests that check the Netty implementation of Reactive IPC.
 */
public class NettyTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(NettyTcpServerIntegrationTests.class);

	@SuppressWarnings("unchecked")
	@Test
	public void canStartNettyTcpServer() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		// This is going to start writing as soon as a request is made, so don't attach it right away.
		Publisher writer = Publishers.just(Unpooled.wrappedBuffer("Hello World!".getBytes()));

		WriteCompleteListener writeCompleteListener = (connection, count, msg) -> {
			// Flush after every write.
			return count > 0;
		};

		ReadCompleteListener readCompleteListener = (connection) -> {
			// only add output writer after everything's been read from the client
			connection.writer(writer);
			// don't close the connection when you've read everything
			return false;
		};

		NettyTcpServer server = NettyTcpServer
				.listen(3000)
				.handler(connection -> connection
						.addListener(writeCompleteListener)
						.addListener(readCompleteListener)
						.reader()
						.subscribe(new Subscriber<Object>() {
							Subscription subscription;

							@Override
							public void onSubscribe(Subscription s) {
								if (null != subscription) {
									s.cancel();
									return;
								}
								this.subscription = s;
								s.request(1);
							}

							@Override
							public void onNext(Object o) {
								LOG.info("got msg: {}", o);
								subscription.request(1);
							}

							@Override
							public void onError(Throwable t) {
								LOG.error(t.getMessage(), t);
							}

							@Override
							public void onComplete() {
								LOG.info("complete");
								latch.countDown();
							}
						}));

		server.start();

		while (!latch.await(1, TimeUnit.SECONDS)) {
			Thread.sleep(1000);
		}

		Thread.sleep(1000);

		server.shutdown();
	}

}
