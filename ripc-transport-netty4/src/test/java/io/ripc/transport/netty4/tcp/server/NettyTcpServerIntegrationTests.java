package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.core.io.Buffer;
import io.ripc.transport.netty4.ByteBufBuffer;
import org.junit.Test;
import org.reactivestreams.Publisher;
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
		CountDownLatch latch = new CountDownLatch(1);

		NettyTcpServer server = NettyTcpServer.listen(3000, conn -> {
			conn.subscribe(new Subscriber<Buffer<ByteBuf>>() {
				private Subscription subscription;

				@Override
				public void onSubscribe(Subscription s) {
					if (null != subscription) {
						s.cancel();
						return;
					}
					(this.subscription = s).request(1);
				}

				@Override
				public void onNext(Buffer<ByteBuf> buffer) {
					subscription.request(1);
				}

				@Override
				public void onError(Throwable t) {
					t.printStackTrace();
				}

				@Override
				public void onComplete() {
					ByteBufBuffer out = new ByteBufBuffer(Unpooled.wrappedBuffer("Hello World".getBytes()), true);

					conn.write(new Publisher<Buffer<ByteBuf>>() {
						@Override
						public void subscribe(Subscriber<? super Buffer<ByteBuf>> s) {
							s.onSubscribe(new Subscription() {
								boolean written;

								@Override
								public void request(long n) {
									if (!written) {
										s.onNext(out);
										written = true;
									} else {
										s.onComplete();
										latch.countDown();
									}
								}

								@Override
								public void cancel() {

								}
							});
						}
					});
				}
			});

		});

		while (!latch.await(1, TimeUnit.SECONDS)) {
			Thread.sleep(1000);
		}

		server.shutdown();
	}

}
