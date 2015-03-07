# Reactive Inter-Process Communication Library

The intent of this project is to bring together Reactive Streams, RxJava, Reactor and similar efforts on networking libraries to create a “networking kernel” that can be the foundation of reactive, stream oriented IO for clients and servers supporting UDP, TCP, HTTP/1, HTTP/2 and WebSockets and a mechanism for pluggable codecs on top of all transports.

The desired timeline is BETA (1.0.rc1) by September 2015 and GA (1.0 Final) by end of 2015.

This would result in 3 layers of modules: API, Transport and Core.

![screen shot 2015-03-06 at 4 06 51 pm](https://cloud.githubusercontent.com/assets/813492/6538742/06420452-c41b-11e4-9467-93d1ac26e25a.png)

Discussion of goals and motiviations can be found in [Issue #1](https://github.com/reactive-ipc/reactive-ipc-jvm/issues/1).
