package io.ripc.composition.reactor;

import io.netty.buffer.ByteBuf;
import io.ripc.composition.reactor.tcp.ReactorTcpServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorTcpServerIntegrationTests.class);

	@Test
	public void reactorTcpServerAcceptsData() throws InterruptedException {
		ReactorTcpServer.listen(3000, ByteBuf.class)
		                .log("connection")
		                .consume(conn -> conn.out(conn.in().log("in"))
		                                     .log("confirmation")
		                                     .consume(buf -> LOG.info("write confirmed: {}", buf)));

		while (true) {
			Thread.sleep(500);
		}
	}

}
