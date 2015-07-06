/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ripc.reactor;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.ripc.reactor.protocol.tcp.ReactorTcpServer;
import io.ripc.test.SocketTestUtils;
import io.ripc.transport.netty4.tcp.Netty4TcpServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import reactor.rx.Promise;
import reactor.rx.Promises;
import reactor.rx.Streams;

public class ReactorTcpServerTests {

    private ReactorTcpServer<ByteBuf, ByteBuf> reactorServer;

    @Before
    public void setup() {
        reactorServer =  ReactorTcpServer.create(Netty4TcpServer.<ByteBuf, ByteBuf>create(0));
    }

    @After
    public void tearDown() {
        reactorServer.shutdown();
    }

    @Test
    public void writeSingleValue() throws IOException {
        reactorServer.start(connection -> connection.writeWith(Streams.just(Unpooled.buffer().writeBytes("test".getBytes()))));
        assertEquals("test", SocketTestUtils.read("localhost", reactorServer.getPort()));
    }

    @Test
    public void writeMultipleValues() throws IOException {
        Promise<ByteBuf> chunk1 = Promises.success(Unpooled.buffer().writeBytes("This is".getBytes()));
        Promise<ByteBuf> chunk2 = Promises.success(Unpooled.buffer().writeBytes(" a test!".getBytes()));
        reactorServer.start(connection -> connection.writeWith(Streams.concat(chunk1, chunk2)));
        assertEquals("This is a test!", SocketTestUtils.read("localhost", reactorServer.getPort()));
    }

}
