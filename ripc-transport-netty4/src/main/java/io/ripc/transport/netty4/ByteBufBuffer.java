package io.ripc.transport.netty4;

import io.netty.buffer.ByteBuf;
import io.ripc.core.io.Buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jbrisbin on 3/10/15.
 */
public class ByteBufBuffer implements Buffer<ByteBuf> {

	private final ByteBuf buf;
	private final boolean writable;

	public ByteBufBuffer(ByteBuf buf, boolean writable) {
		this.buf = buf;
		this.writable = writable;
	}

	@Override
	public int position() {
		return (writable ? buf.writerIndex() : buf.readerIndex());
	}

	@Override
	public Buffer<ByteBuf> position(int pos) {
		return null;
	}

	@Override
	public int limit() {
		return (writable ? buf.writableBytes() : buf.readableBytes());
	}

	@Override
	public Buffer<ByteBuf> limit(int limit) {
		return null;
	}

	@Override
	public int capacity() {
		return buf.capacity();
	}

	@Override
	public Buffer<ByteBuf> capacity(int capacity) {
		buf.capacity(capacity);
		return this;
	}

	@Override
	public int remaining() {
		return capacity() - position();
	}

	@Override
	public Buffer<ByteBuf> skip(int len) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> clear() {
		return null;
	}

	@Override
	public Buffer<ByteBuf> compact() {
		return null;
	}

	@Override
	public Buffer<ByteBuf> flip() {
		return null;
	}

	@Override
	public Buffer<ByteBuf> rewind() {
		return null;
	}

	@Override
	public Buffer<ByteBuf> rewind(int len) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> clone() {
		return null;
	}

	@Override
	public Buffer<ByteBuf> copy() {
		return new ByteBufBuffer(buf.copy(), writable);
	}

	@Override
	public Buffer<ByteBuf> slice(int start, int len) {
		return new ByteBufBuffer(buf.copy(start, len), writable);
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(byte delimiter) {
		return null;
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(byte delimiter, boolean stripDelimiter) {
		return null;
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(byte delimiter,
	                                       boolean stripDelimiter,
	                                       List<Buffer<ByteBuf>> preallocatedList) {
		return null;
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(Buffer<ByteBuf> delimiter) {
		return null;
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(Buffer<ByteBuf> delimiter, boolean stripDelimiter) {
		return null;
	}

	@Override
	public Iterable<Buffer<ByteBuf>> split(Buffer<ByteBuf> delimiter,
	                                       boolean stripDelimiter,
	                                       List<Buffer<ByteBuf>> preallocatedList) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(ByteBuf data) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(Buffer<ByteBuf> buffer) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(ByteBuffer buffer) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(CharSequence chars) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(byte[] bytes) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(byte b) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(char c) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(short s) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(int i) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> prepend(long l) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(ByteBuf data) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(Buffer<ByteBuf> buffer) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(ByteBuffer buffer) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(CharSequence chars) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(byte[] bytes) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(byte b) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(char c) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(short s) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(int i) {
		return null;
	}

	@Override
	public Buffer<ByteBuf> append(long l) {
		return null;
	}

	@Override
	public byte readByte() {
		return 0;
	}

	@Override
	public void readBytes(byte[] bytes) {

	}

	@Override
	public short readShort() {
		return 0;
	}

	@Override
	public int readInt() {
		return 0;
	}

	@Override
	public float readFloat() {
		return 0;
	}

	@Override
	public double readDouble() {
		return 0;
	}

	@Override
	public long readLong() {
		return 0;
	}

	@Override
	public char readChar() {
		return 0;
	}

	@Override
	public void readChars(char[] chars) {

	}

	@Override
	public String readString() {
		CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
		try {
			CharBuffer cb = decoder.decode(buf.nioBuffer());
			return cb.toString();
		} catch (CharacterCodingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String readString(CharsetDecoder decoder) {
		return null;
	}

	@Override
	public ByteBuf get() {
		return buf;
	}

	@Override
	public void close() throws Exception {

	}

	@Override
	public int compareTo(Buffer<ByteBuf> o) {
		return 0;
	}

	@Override
	public Iterator<Buffer<ByteBuf>> iterator() {
		return null;
	}

	@Override
	public String toString() {
		return "ByteBufBuffer{" +
				"buf=" + buf +
				", writable=" + writable +
				'}';
	}
	
}
