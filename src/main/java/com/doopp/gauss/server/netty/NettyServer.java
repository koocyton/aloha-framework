package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.util.JarToolUtil;
import com.google.inject.Inject;
import com.google.inject.Injector;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class NettyServer {

    @Inject
    private Injector injector;

    @Inject
    private HelloHandle helloHandle;

    public void run() throws URISyntaxException {

        System.out.print("\n" + getClass().getResource("") + "\n");
        System.out.print("\n" + getClass().getResource("/resources") + "\n");
        System.out.print("\n" + getClass().getResource("/resources/public") + "\n");
        System.out.print("\n" + getClass().getResourceAsStream("/public") + "\n");
        System.out.print("\n" + getClass().getResource("/public") + "\n");

        Path resource = JarToolUtil.getJarName().contains("jar")
                ? Paths.get(getClass().getResource("/resources/public").toURI())
                : Paths.get(getClass().getResource("/public").toURI());
        Consumer<HttpServerRoutes> routesConsumer = routes -> routes
                .get("/hello", (req, res) -> res.sendString(
                        helloHandle.hello()
                ))
                .get("/boy", (req, res) -> res.sendString(
                        helloHandle.boy(req)
                ))
                .ws("/game", (in, out) -> out.send(
                        helloHandle.game(in.receive())
                ))
                .directory("/", resource);

        DisposableServer disposableServer = HttpServer.create()
                .route(routesConsumer)
                .host("127.0.0.1")
                .port(8090)
                .bind()
                .block();

        disposableServer.onDispose().block();
    }
}



