package io.ripc.protocol.tcp;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TcpServer<R, W> {

    protected final TcpHandler<R, W> thisHandler;
    protected final AtomicBoolean started;

    protected TcpServer() {
        thisHandler = null;
        this.started = new AtomicBoolean();
    }

    public final TcpServer<R, W> start(TcpHandler<R, W> handler) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        doStart(handler);
        return this;
    }

    public final void startAndAwait(TcpHandler<R, W> handler) {
        start(handler);
        awaitShutdown();
    }

    public final boolean shutdown() {
        return !started.compareAndSet(true, false) || doShutdown();
    }

    public abstract void awaitShutdown();

    public abstract boolean doShutdown();

    protected abstract TcpServer<R, W> doStart(TcpHandler<R, W> handler);

    public abstract int getPort();

}
