package io.ripc.reactor.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.protocol.tcp.TcpServer;
import io.ripc.transport.netty4.tcp.Netty4TcpServer;
import reactor.rx.Promise;
import reactor.rx.Promises;
import reactor.rx.Streams;

import java.nio.charset.Charset;

/**
 * Created by jbrisbin on 5/28/15.
 */
public class ReactorTcpServerSample {

    public static void main(String... args) throws InterruptedException {
        TcpServer<ByteBuf, ByteBuf> transport = Netty4TcpServer.<ByteBuf, ByteBuf>create(0);
//        echo(transport);
        echoWithQuitCommand(transport);
    }

    /**
     * Keep echoing until the client goes away.
     */
    private static void echo(TcpServer<ByteBuf, ByteBuf> transport) {
        ReactorTcpServer.create(transport)
                        .startAndAwait(connection -> {
                            connection.flatMap(inByteBuf -> {
                                String text = "Hello " + inByteBuf.toString(Charset.defaultCharset());
                                ByteBuf outByteBuf = Unpooled.buffer().writeBytes(text.getBytes());
                                return connection.writeWith(Streams.just(outByteBuf));
                            }).consume();
                            return Streams.never();
                        });
    }

    /**
     * Keep echoing until the client sends "quite".
     */
    private static void echoWithQuitCommand(TcpServer<ByteBuf, ByteBuf> transport) {
        ReactorTcpServer.create(transport)
                .start(connection -> {
                    Promise<Void> promise = Promises.prepare();
                    connection.flatMap(inByteBuf -> {
                        String input = inByteBuf.toString(Charset.defaultCharset()).trim();
                        if ("quit".equalsIgnoreCase(input)) {
                            promise.onComplete();
                            return promise;
                        }
                        else {
                            String text = "Hello " + inByteBuf.toString(Charset.defaultCharset());
                            ByteBuf outByteBuf = Unpooled.buffer().writeBytes(text.getBytes());
                            return connection.writeWith(Streams.just(outByteBuf));
                        }
                    }).consume();
                    return promise;
                });
    }

}