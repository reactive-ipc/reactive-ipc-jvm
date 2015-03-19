package io.rpc.rx.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.rx.protocol.tcp.RxTcpInterceptor;
import io.ripc.rx.protocol.tcp.RxTcpServer;
import io.ripc.transport.netty4.tcp.Netty4TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.Charset.*;

public class RxTcpServerSample {

    private static final Logger logger = LoggerFactory.getLogger(RxTcpServerSample.class);

    public static RxTcpInterceptor<ByteBuf, ByteBuf, ByteBuf, ByteBuf> log() {
        return handler -> input -> {
            logger.error("Received a new connection.");
            return handler.handle(input);
        };
    }

    public static void main(String[] args) {

        TcpServer<ByteBuf, ByteBuf> transport = Netty4TcpServer.<ByteBuf, ByteBuf>create(0);

        RxTcpServer.create(transport)
                   .intercept(log())
                   .startAndAwait(connection -> connection.write(connection.map(bb -> {
                       String msg = "Hello " + bb.toString(defaultCharset());
                       return Unpooled.buffer().writeBytes(msg.getBytes());
                   }), (count, item) -> true));
    }
}