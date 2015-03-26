package io.ripc.transport.netty4.tcp.server;

import io.netty.buffer.Unpooled;
import io.ripc.core.Publishers;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
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
		CountDownLatch latch = new CountDownLatch(1);

		WriteCompleteListener writeCompleteListener = new WriteCompleteListener() {
			@Override
			public boolean writeComplete(TcpConnection connection, long count, Object msg) {
				return count > 0;
			}
		};

		NettyTcpServer server = NettyTcpServer.listen(3000, connection ->
				connection.addListener(writeCompleteListener)
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
						          connection.writer(Publishers.just(Unpooled.wrappedBuffer("Hello World!".getBytes())));
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

		while (!latch.await(1, TimeUnit.SECONDS)) {
			Thread.sleep(1000);
		}

		server.shutdown();
	}

}
