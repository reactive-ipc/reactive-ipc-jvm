package io.ripc.transport.netty4.tcp;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.internal.Publishers;

import static java.nio.charset.Charset.*;
import static rx.RxReactiveStreams.*;

public class TcpServerSample {

    public static void main(String[] args) {
        Netty4TcpServer.<ByteBuf, ByteBuf>create(0)
                       .start(connection ->
                                      toPublisher(toObservable(connection)
                                                          .flatMap(byteBuf -> {
                                                              String msg = "Hello " + byteBuf.toString(defaultCharset());
                                                              ByteBuf toWrite = Unpooled.buffer().writeBytes(msg.getBytes());
                                                              return toObservable(connection.write(Publishers.just(toWrite)));
                                                          })));
    }
}