package io.ripc.composition.reactor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.composition.reactor.tcp.ReactorTcpServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.rx.Streams;

import java.nio.charset.Charset;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpServerIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorTcpServerIntegrationTests.class);

	@Test
	public void reactorTcpServerAcceptsData() throws InterruptedException {
		ReactorTcpServer.listen(3000, ByteBuf.class)
		                .log("connection")
		                .consume(conn -> {
			                conn.in()
			                    .log("in")
			                    .map(buf -> buf.toString(Charset.defaultCharset()))
			                    .consume(s -> {
				                    LOG.info("received {}: {}", s.getClass(), s);
			                    });

			                conn.out(Streams.just("Hello World!")
			                                .log("out")
			                                .map(s -> Unpooled.wrappedBuffer(s.getBytes())));
		                });

		while (true) {
			Thread.sleep(500);
		}
	}

}
