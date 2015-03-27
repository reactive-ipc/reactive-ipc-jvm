package io.ripc.transport.netty4.tcp;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.internal.Publishers;
import io.ripc.protocol.tcp.TcpInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.Charset.defaultCharset;
import static rx.RxReactiveStreams.*;

public class TcpServerSample {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerSample.class);

    public static TcpInterceptor<ByteBuf, ByteBuf, ByteBuf, ByteBuf> log() {
        return handler -> input -> {
            logger.error("Received a new connection.");
            return handler.handle(input);
        };
    }

    public static TcpInterceptor<ByteBuf, ByteBuf, ByteBuf, ByteBuf> shortCircuitAltConnection() {
        return handler -> {
            final AtomicLong connCounter = new AtomicLong();
            return input -> {
                if (connCounter.incrementAndGet() % 2 == 0) {
                    logger.error("Short-circuiting further processing.");
                    return input.write(Publishers.just(Unpooled.buffer().writeBytes("Go Away!!! \n".getBytes())));
                }
                return handler.handle(input);
            };
        };
    }

    public static void main(String[] args) {
        Netty4TcpServer.<ByteBuf, ByteBuf>create(0)
                       .intercept(log())
                       .intercept(shortCircuitAltConnection())
                       .start(connection ->
                                      toPublisher(toObservable(connection)
                                                          .flatMap(byteBuf -> {
                                                              String msg = "Hello " + byteBuf.toString(defaultCharset());
                                                              ByteBuf toWrite = Unpooled.buffer().writeBytes(msg.getBytes());
                                                              return toObservable(connection.write(Publishers.just(toWrite)));
                                                          })));
    }
}