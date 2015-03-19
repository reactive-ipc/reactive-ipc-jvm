package io.ripc.protocol.tcp;

import io.ripc.internal.Publishers;
import org.reactivestreams.Publisher;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TcpServer<R, W> {

    @SuppressWarnings("rawtypes")
    private LazyTcpHandler lazyHandler;
    protected final TcpHandler<R, W> thisHandler;
    protected final AtomicBoolean started;

    protected TcpServer() {
        lazyHandler = new LazyTcpHandler<>();
        thisHandler = null;
        this.started = new AtomicBoolean();
    }

    protected TcpServer(TcpHandler<R, W> handler) {
        thisHandler = handler;
        this.started = new AtomicBoolean();
    }

    public <RR, WW> TcpServer<RR, WW> intercept(TcpInterceptor<R, W, RR, WW> interceptor) {
        if (null == thisHandler) {
            @SuppressWarnings("unchecked")
            TcpServer<RR, WW> toReturn = newServer(interceptor.intercept(lazyHandler));
            toReturn.lazyHandler = lazyHandler;
            return toReturn;
        } else {
            TcpServer<RR, WW> toReturn = newServer(interceptor.intercept(thisHandler));
            toReturn.lazyHandler = lazyHandler;
            return toReturn;
        }
    }

    public final TcpServer<R, W> start(TcpHandler<R, W> handler) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        if (null == thisHandler) {
            doStart(handler);
        } else {
            lazyHandler.start(handler);
            doStart(thisHandler);
        }
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

    protected abstract <RR, WW> TcpServer<RR, WW> newServer(TcpHandler<RR, WW> handler);

    protected abstract TcpServer<R, W> doStart(TcpHandler<R, W> handler);

    private static class LazyTcpHandler<R, W> implements TcpHandler<R, W> {

        private TcpHandler<R, W> delegate;

        @Override
        public Publisher<Void> handle(Connection<R, W> connection) {
            if (null == delegate) {
                return Publishers.error(new IllegalStateException("Handler not initialized."));
            } else {
                return delegate.handle(connection);
            }
        }

        @SuppressWarnings("unchecked")
        private void start(TcpHandler handler) {
            delegate = handler;
        }
    }
}
