package io.ripc.protocol.tcp;

public interface TcpInterceptor<I, O, II, OO> {

    TcpHandler<II, OO> intercept(TcpHandler<I, O> handler);

}
