package io.ripc.protocol.tcp;

import io.ripc.core.io.Buffer;
import org.reactivestreams.Publisher;

/**
 * Created by jbrisbin on 3/10/15.
 */
public interface Connection<B> extends Publisher<Buffer<B>> {

	void write(Publisher<Buffer<B>> data);

}
