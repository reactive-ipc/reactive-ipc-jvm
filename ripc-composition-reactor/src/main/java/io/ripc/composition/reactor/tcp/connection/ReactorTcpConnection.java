package io.ripc.composition.reactor.tcp.connection;

import io.netty.channel.ChannelHandlerContext;
import io.ripc.composition.reactor.tcp.ReactorBufferChannelHandler;
import io.ripc.protocol.tcp.connection.TcpConnection;
import io.ripc.protocol.tcp.connection.listener.WriteCompleteListener;
import io.ripc.transport.netty4.listener.ChannelActiveListener;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

/**
 * Created by jbrisbin on 3/27/15.
 */
public class ReactorTcpConnection<R, W> {

	private final Broadcaster<Object> writeComplete = Broadcaster.create();

	private final TcpConnection       connection;
	private final Codec<Buffer, R, W> codec;
	private final Stream<R>           in;

	@SuppressWarnings("unchecked")
	public ReactorTcpConnection(TcpConnection connection, Codec<Buffer, R, W> codec) {
		this.connection = connection;
		this.codec = codec;

		this.connection.addListener(new WriteCompleteListener() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean writeComplete(TcpConnection connection, long count, Object msg) {
				writeComplete.onNext(msg);
				return true;
			}
		});

		this.connection.addListener(new ChannelActiveListener() {
			@Override
			public void channelActive(ChannelHandlerContext ctx) {
				ctx.pipeline().addBefore("reactive-ipc-inbound",
				                         "byteBufToBufferHandler",
				                         ReactorBufferChannelHandler.INSTANCE);
			}
		});

		this.in = Streams.wrap(connection.reader())
		                 .map(new Function<Object, R>() {
			                 Function<Buffer, R> decoder = (null != codec ? codec.decoder() : null);

			                 @Override
			                 public R apply(Object o) {
				                 return (null != decoder ? decoder.apply((Buffer) o) : (R) o);
			                 }
		                 });
	}

	public Stream<R> in() {
		return in;
	}

	public Stream<Object> out(Stream<W> out) {
		connection.writer(out.observeComplete(v -> writeComplete.onComplete())
		                     .when(Throwable.class, writeComplete::onError)
		                     .map(codec.encoder()));
		return writeComplete;
	}

}
