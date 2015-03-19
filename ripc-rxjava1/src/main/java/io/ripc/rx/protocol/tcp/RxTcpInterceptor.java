package io.ripc.rx.protocol.tcp;

public interface RxTcpInterceptor<R, W, RR, WW> {

    RxTcpHandler<RR, WW> intercept(RxTcpHandler<R, W> handler);
}
