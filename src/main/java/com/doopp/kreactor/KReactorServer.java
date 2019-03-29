package com.doopp.kreactor;

import com.google.inject.Injector;
import reactor.netty.http.server.HttpServer;

public class KReactorServer {

    private static final KReactorServer INSTANCE = new KReactorServer();

    private static final Dispatcher DISPATCHER = new Dispatcher();

    private String host = "127.0.0.1";

    private int port = 8081;

    public static KReactorServer create() {
        return INSTANCE;
    }

    public KReactorServer bind(String host, int port) {
        INSTANCE.host = host;
        INSTANCE.port = port;
        return INSTANCE;
    }

    public KReactorServer basePackages(String... basePackages) {
        DISPATCHER.setHandlePackages(basePackages);
        return INSTANCE;
    }

    public KReactorServer injector(Injector guiceInjector) {
        DISPATCHER.setInjector(guiceInjector);
        return INSTANCE;
    }

    public KReactorServer addFilter(String path, KReactorFilter filter) {
        DISPATCHER.addFilter(path, filter);
        return INSTANCE;
    }

    public void launch() {
         HttpServer.create()
                .route(DISPATCHER.routesBuilder())
                .host(INSTANCE.host)
                .port(INSTANCE.port)
                .wiretap(true)
                .bindNow()
                .onDispose()
                .block();
    }
}
