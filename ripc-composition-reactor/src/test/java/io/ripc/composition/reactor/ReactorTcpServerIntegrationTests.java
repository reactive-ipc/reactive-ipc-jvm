package io.ripc.composition.reactor;

import io.ripc.composition.reactor.tcp.ReactorTcpServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.codec.StandardCodecs;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for Reactor implementations of RIPC components.
 */
public class ReactorTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorTcpServerIntegrationTests.class);

	@Test
	public void reactorTcpServerAcceptsData() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		ReactorTcpServer<String, String> server = ReactorTcpServer.listen(3000, StandardCodecs.STRING_CODEC);

		server.log("connection")
		      .consume(conn -> conn.out(conn.in()
		                                    .log("in"))
		                           .log("confirmation")
		                           .observeComplete(v -> latch.countDown())
		                           .consume(buf -> LOG.info("write confirmed: {}", buf)));

		while (!latch.await(1, TimeUnit.SECONDS)) {
		}

		server.shutdown();
	}

}
