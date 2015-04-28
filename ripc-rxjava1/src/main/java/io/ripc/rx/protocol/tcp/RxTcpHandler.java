package io.ripc.rx.protocol.tcp;

import rx.Observable;

public interface RxTcpHandler<R, W> {

    Observable<Void> handle(RxConnection<R, W> connection);
}
