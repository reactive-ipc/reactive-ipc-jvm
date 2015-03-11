package io.ripc.core.io;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.List;

/**
 * Common abstraction to provide additional functionality beyond a traditional {@link java.nio.ByteBuffer} while not
 * restricting the dedicated functionality provided by concrete implementations from various transport libraries that
 * might offer features like zero-copy.
 * <p>
 * A {@code Buffer} can be anything. It is not limited to byte buffers. A {@code Buffer} could represent realized
 * objects descended from raw data.
 * </p>
 */
public interface Buffer<B> extends Cloneable,
                                   AutoCloseable,
                                   Comparable<Buffer<B>>,
                                   Iterable<Buffer<B>> {

	int position();

	Buffer<B> position(int pos);

	int limit();

	Buffer<B> limit(int limit);

	int capacity();

	Buffer<B> capacity(int capacity);

	int remaining();

	Buffer<B> skip(int len);

	Buffer<B> clear();

	Buffer<B> compact();

	Buffer<B> flip();

	Buffer<B> rewind();

	Buffer<B> rewind(int len);

	Buffer<B> clone();

	Buffer<B> copy();

	Buffer<B> slice(int start, int len);

	Iterable<Buffer<B>> split(byte delimiter);

	Iterable<Buffer<B>> split(byte delimiter, boolean stripDelimiter);

	Iterable<Buffer<B>> split(byte delimiter, boolean stripDelimiter, List<Buffer<B>> preallocatedList);

	Iterable<Buffer<B>> split(Buffer<B> delimiter);

	Iterable<Buffer<B>> split(Buffer<B> delimiter, boolean stripDelimiter);

	Iterable<Buffer<B>> split(Buffer<B> delimiter, boolean stripDelimiter, List<Buffer<B>> preallocatedList);

	Buffer<B> prepend(B data);

	Buffer<B> prepend(Buffer<B> buffer);

	Buffer<B> prepend(ByteBuffer buffer);

	Buffer<B> prepend(CharSequence chars);

	Buffer<B> prepend(byte[] bytes);

	Buffer<B> prepend(byte b);

	Buffer<B> prepend(char c);

	Buffer<B> prepend(short s);

	Buffer<B> prepend(int i);

	Buffer<B> prepend(long l);

	Buffer<B> append(B data);

	Buffer<B> append(Buffer<B> buffer);

	Buffer<B> append(ByteBuffer buffer);

	Buffer<B> append(CharSequence chars);

	Buffer<B> append(byte[] bytes);

	Buffer<B> append(byte b);

	Buffer<B> append(char c);

	Buffer<B> append(short s);

	Buffer<B> append(int i);

	Buffer<B> append(long l);

	byte readByte();

	void readBytes(byte[] bytes);

	short readShort();

	int readInt();

	float readFloat();

	double readDouble();

	long readLong();

	char readChar();

	void readChars(char[] chars);

	String readString();

	String readString(CharsetDecoder decoder);

	B get();

}
