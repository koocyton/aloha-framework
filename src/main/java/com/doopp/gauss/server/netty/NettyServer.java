package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.AppRoutes;
import com.google.inject.Inject;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import java.net.URISyntaxException;

public class NettyServer {

    @Inject
    private AppRoutes appRoutes;

    public void run() throws URISyntaxException {

        DisposableServer disposableServer = HttpServer.create()
                .route(appRoutes.getRoutesConsumer())
                .host("127.0.0.1")
                .port(8090)
                .bind()
                .block();

        disposableServer.onDispose().block();
    }
}



