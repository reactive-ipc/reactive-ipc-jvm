package io.ripc.protocol.tcp;

import org.reactivestreams.Publisher;

/**
 * Created by jbrisbin on 3/10/15.
 */
public interface ConnectionPublisher<B> extends Publisher<Connection<B>> {
}
