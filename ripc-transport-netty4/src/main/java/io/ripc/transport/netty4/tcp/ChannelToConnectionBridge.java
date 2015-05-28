package io.ripc.transport.netty4.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.ripc.protocol.tcp.TcpHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bridge between netty's {@link Channel} and {@link io.ripc.protocol.tcp.TcpConnection}. It has the following responsibilities:
 *
 * <ul>
 <li>Create a new {@link io.ripc.protocol.tcp.TcpConnection} instance when the channel is active and forwards it to the configured
 {@link TcpHandler}.</li>
 <li>Reads any data from the channel and forwards it to the {@link Subscriber} attached via the event
 {@link ChannelToConnectionBridge.ConnectionInputSubscriberEvent}</li>
 <li>Accepts writes of {@link Publisher} on the channel and translates the items emitted from that publisher to the
 channel.</li>
 </ul>
 *
 * @param <R> The type of objects read from the underneath channel.
 * @param <W> The type of objects read written to the underneath channel.
 */
public class ChannelToConnectionBridge<R, W> extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChannelToConnectionBridge.class);

    private final TcpHandler<R, W> handler;
    private TcpConnectionImpl<R, W> conn;
    private Subscriber<R> inputSubscriber; /*Populated via event ConnectionInputSubscriberEvent*/

    public ChannelToConnectionBridge(TcpHandler<R, W> handler) {
        this.handler = handler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        conn = new TcpConnectionImpl<>(ctx.channel());
        handler.handle(conn)
               .subscribe(new Subscriber<Void>() {
                   @Override
                   public void onSubscribe(Subscription s) {
                       // Void, no op
                   }

                   @Override
                   public void onNext(Void aVoid) {
                       // Void, no op
                   }

                   @Override
                   public void onError(Throwable t) {
                       logger.error("Error processing connection. Closing the channel.", t);
                       ctx.channel().close();
                   }

                   @Override
                   public void onComplete() {
                       ctx.channel().close();
                   }
               });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == conn || null == inputSubscriber) {
            logger.error("No connection input subscriber available. Disposing data.");
            ReferenceCountUtil.release(msg);
            return;
        }

        try {
            inputSubscriber.onNext((R) msg);
        } catch (ClassCastException e) {
            logger.error("Invalid message type read from the pipeline.", e);
            inputSubscriber.onError(e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (null != conn && inputSubscriber != null) {
            inputSubscriber.onComplete();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof ConnectionInputSubscriberEvent) {
            @SuppressWarnings("unchecked")
            ConnectionInputSubscriberEvent<R> subscriberEvent = (ConnectionInputSubscriberEvent<R>) evt;
            if (null == inputSubscriber) {
                inputSubscriber = subscriberEvent.getInputSubscriber();
            } else {
                inputSubscriber.onError(new IllegalStateException("Only one connection input subscriber allowed."));
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, final ChannelPromise promise) throws Exception {
        if (msg instanceof Publisher) {
            @SuppressWarnings("unchecked")
            final Publisher<W> data = (Publisher<W>) msg;

            data.subscribe(new Subscriber<W>() {

                // TODO: Needs to be fixed to wire all futures to the promise of the Publisher write.
                private ChannelFuture lastWriteFuture;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE); // TODO: Backpressure
                }

                @Override
                public void onNext(W w) {
                    lastWriteFuture = ctx.channel().write(w);
                }

                @Override
                public void onError(Throwable t) {
                    onTerminate();
                }

                @Override
                public void onComplete() {
                    onTerminate();
                }

                private void onTerminate() {
                    ctx.channel().flush();
                    lastWriteFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                promise.trySuccess();
                            } else {
                                promise.tryFailure(future.cause());
                            }
                        }
                    });
                }
            });
        } else {
            super.write(ctx, msg, promise);
        }
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(),cause);
	}

	/**
     * An event to attach a {@link Subscriber} to the {@link io.ripc.protocol.tcp.TcpConnection} created by {@link ChannelToConnectionBridge}
     *
     * @param <R>
     */
    public static final class ConnectionInputSubscriberEvent<R> {

        private final Subscriber<R> inputSubscriber;

        public ConnectionInputSubscriberEvent(Subscriber<R> inputSubscriber) {
            if (null == inputSubscriber) {
                throw new IllegalArgumentException("Connection input subscriber must not be null.");
            }
            this.inputSubscriber = inputSubscriber;
        }

        public Subscriber<R> getInputSubscriber() {
            return inputSubscriber;
        }
    }
}
