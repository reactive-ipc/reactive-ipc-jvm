package io.ripc.composition.reactor;

import io.netty.buffer.ByteBuf;
import io.ripc.composition.reactor.tcp.ReactorTcpServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorTcpServerIntegrationTests.class);

	@Test
	public void reactorTcpServerAcceptsData() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		ReactorTcpServer<ByteBuf> server = ReactorTcpServer.listen(3000, ByteBuf.class);

		server.log("connection")
		      .consume(conn -> conn.out(conn.in()
		                                    .log("in")
		                                    .observeComplete(v -> latch.countDown()))
		                           .log("confirmation")
		                           .consume(buf -> LOG.info("write confirmed: {}", buf)));

		while (!latch.await(1, TimeUnit.SECONDS)) {
			Thread.sleep(500);
		}

		Thread.sleep(500);

		server.shutdown();
	}

}
