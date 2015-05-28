package io.ripc.reactor.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.transport.netty4.tcp.Netty4TcpServer;
import reactor.rx.Streams;

import java.nio.charset.Charset;

/**
 * Created by jbrisbin on 5/28/15.
 */
public class ReactorTcpServerSample {
	public static void main(String... args) throws InterruptedException {
		TcpServer<ByteBuf, ByteBuf> transport = Netty4TcpServer.<ByteBuf, ByteBuf>create(0);

		ReactorTcpServer.create(transport)
		                .start(ch -> ch.flatMap(bb -> {
			                String msgStr = "Hello " + bb.toString(Charset.defaultCharset()) + "!";
			                ByteBuf msg = Unpooled.buffer().writeBytes(msgStr.getBytes());
			                return ch.writeWith(Streams.just(msg));
		                }));
	}

}
