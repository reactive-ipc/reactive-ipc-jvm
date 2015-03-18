package io.ripc.protocol.tcp;

import io.ripc.core.Consumer;
import io.ripc.core.Supplier;

/**
 * A {@code ConnectionHandler} is responsible for composing a Reactive Streams pipeline(s) when a new connection is
 * received by the server. Implementations will compose an appropriate pipeline based on capabilities and server
 * configuration.
 */
public interface ConnectionHandler<B> extends Supplier<Consumer<Connection<B>>> {
}
