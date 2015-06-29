package io.ripc.transport.netty4.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.ripc.internal.Publishers;
import io.ripc.protocol.tcp.TcpServer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class CodecSample {

    private static final Logger LOG = LoggerFactory.getLogger(CodecSample.class);


    public static void main(String... args) {
        echoWithLineBasedFrameDecoder();
//        echoJsonStreamDecoding();
    }

    private static void echoWithLineBasedFrameDecoder() {

        TcpServer<String, String> server = Netty4TcpServer.create(
                0,
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        int bufferSize = 1024;
                        ChannelConfig config = channel.config();
                        config.setOption(ChannelOption.SO_RCVBUF, bufferSize);
                        config.setOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(bufferSize));
                        channel.pipeline().addFirst(
                                new LineBasedFrameDecoder(256),
                                new StringDecoder(CharsetUtil.UTF_8),
                                new StringEncoder(CharsetUtil.UTF_8));
                    }
                });

        server.start(conn -> {
            conn.subscribe(new Subscriber<String>() {

                private Subscription subscription;

                @Override
                public void onSubscribe(Subscription s) {
                    this.subscription = s;
                    LOG.info("requesting 1...");
                    s.request(1);
                }

                @Override
                public void onNext(String s) {
                    LOG.info("onNext: {}", s);
                    conn.write(Publishers.just("Hello " + s + "\n"))
                            .subscribe(new Subscriber<Void>() {

                                @Override
                                public void onSubscribe(Subscription s) {
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                }

                                @Override
                                public void onError(Throwable t) {
                                }

                                @Override
                                public void onComplete() {
                                    LOG.info("requesting 1...");
                                    subscription.request(1);
                                }
                            });
                }

                @Override
                public void onError(Throwable t) {
                    LOG.error(t.getMessage(), t);
                }

                @Override
                public void onComplete() {
                    LOG.info("onComplete");
                }
            });

            return Publishers.just(null);
        });

        server.awaitShutdown();
    }

    private static void echoJsonStreamDecoding() {

        TcpServer<Person, Person> server = Netty4TcpServer.<Person, Person>create(
                0,
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline()
                               .addFirst(new JsonObjectDecoder(),
                                         new JacksonJsonCodec());
                    }
                });

        server.start(conn -> {
            conn.subscribe(new Subscriber<Person>() {

                private Subscription subscription;

                @Override
                public void onSubscribe(Subscription s) {
                    this.subscription = s;
                    LOG.info("requesting 1...");
                    s.request(1);
                }

                @Override
                public void onNext(Person p) {
                    LOG.info("onNext: {}", p);
                    conn.write(Publishers.just(new Person(p.getLastName(), p.getFirstName())))
                            .subscribe(new Subscriber<Void>() {

                                @Override
                                public void onSubscribe(Subscription s) {
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                }

                                @Override
                                public void onError(Throwable t) {
                                }

                                @Override
                                public void onComplete() {
                                    LOG.info("requesting 1...");
                                    subscription.request(1);
                                }
                            });
                }

                @Override
                public void onError(Throwable t) {
                    LOG.error(t.getMessage(), t);
                }

                @Override
                public void onComplete() {
                    LOG.info("onComplete");
                }
            });

            return Publishers.just(null);
        });

        server.awaitShutdown();
    }

    private static class JacksonJsonCodec extends ChannelDuplexHandler {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
            if (message instanceof ByteBuf) {
                Charset charset = Charset.defaultCharset();
                message = this.mapper.readValue(((ByteBuf) message).toString(charset), Person.class);
            }
            super.channelRead(context, message);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof Person) {
                byte[] buff = mapper.writeValueAsBytes(msg);
                ByteBuf bb = ctx.alloc().buffer(buff.length);
                bb.writeBytes(buff);
                msg = bb;
            }
            super.write(ctx, msg, promise);
        }
    }

    private static class Person {

        private String firstName;

        private String lastName;

        public Person() {
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public Person setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public Person setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
    }

}
