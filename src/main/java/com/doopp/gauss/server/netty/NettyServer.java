package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.util.JarToolUtil;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class NettyServer {

    @Inject
    private Injector injector;

    public void run() {

        Consumer<HttpServerRoutes> routesConsumer = routes -> routes
                .get("/hello", (req, res) -> res.sendString(
                        injector.getInstance(HelloHandle.class).hello()
                ))
                .get("/boy", (req, res) -> res.sendString(
                        injector.getInstance(HelloHandle.class).boy(req)
                ))
                .ws("/game", (in, out) -> out.send(
                        injector.getInstance(HelloHandle.class).game(in.receive())
                ))
                .directory("/", Paths.get("D:\\project\\aloha-framework\\src\\main\\resources\\public"));

        DisposableServer disposableServer = HttpServer.create()
                .route(routesConsumer)
                .host("127.0.0.1")
                .port(8090)
                .bind()
                .block();

        disposableServer.onDispose().block();
    }
}



