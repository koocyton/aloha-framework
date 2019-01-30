package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.util.JarToolUtil;
import com.google.inject.Inject;
import reactor.netty.http.server.HttpServerRoutes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class AppRoutes {

    @Inject
    private HelloHandle helloHandle;

    public Consumer<HttpServerRoutes> getRoutesConsumer() {

        System.out.print("\n" + getClass().getResource("") + "\n");
        System.out.print("\n" + getClass().getResource("/resources") + "\n");
        System.out.print("\n" + getClass().getResource("/resources/public") + "\n");
        System.out.print("\n" + getClass().getResourceAsStream("/public") + "\n");
        System.out.print("\n" + getClass().getResource("/public") + "\n");

        try {
            Path resource = JarToolUtil.getJarName().contains("jar")
                ? Paths.get(getClass().getResource("/resources/public").toURI())
                : Paths.get(getClass().getResource("/public").toURI());
        }
        catch(Exception e ) {

        }
        return routes -> routes
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
    }
}
