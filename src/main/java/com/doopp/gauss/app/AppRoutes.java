package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.util.JarToolUtil;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRoutes;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class AppRoutes {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    @Inject
    private HelloHandle helloHandle;

    public Consumer<HttpServerRoutes> getRoutesConsumer() throws URISyntaxException {

        // logger.info("{}", getClass().getResource(""));
        // logger.info("{}", getClass().getResource("/resources"));
        // logger.info("{}", getClass().getResource("/resources/public"));
        logger.info("{}", getClass().getResourceAsStream("/public"));
        // logger.info("{}", getClass().getResource("/public"));

        // String relativePath = JarToolUtil.getJarName().contains("jar") ? "/resources/public" : "/public";
        Path publicPath = Paths.get(getClass().getResource("/public").toURI());

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
            .directory("/", publicPath);
    }
}
