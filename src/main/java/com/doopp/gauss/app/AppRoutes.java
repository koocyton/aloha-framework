package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRoutes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Consumer;

public class AppRoutes {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    @Inject
    private HelloHandle helloHandle;

    public Consumer<HttpServerRoutes> getRoutesConsumer() throws IOException {

        // logger.info("{}", getClass().getResource(""));
        // logger.info("{}", getClass().getResource("/resources"));
        // logger.info("{}", getClass().getResource("/resources/public"));
        // logger.info("{}", getClass().getResourceAsStream("/public"));
        // logger.info("{}", getClass().getResource("/public"));
        logger.info("{}", resolveContentPath());

        Path contentPath = resolveContentPath();

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
            .directory("/", contentPath);
    }

    private Path resolveContentPath() throws IOException {
        URI cp = null;

        try {
            cp = getClass().getResource("/public").toURI();
            logger.info("{}", cp);
        }
        catch (Exception e) {
            logger.info("{}", cp);
        }

        if (cp!=null) {
            return Paths.get(cp);
        }
        FileSystem fs = FileSystems.newFileSystem(cp, Collections.emptyMap());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try{
                fs.close();
            }
            catch (IOException io){
                //ignore
            }
        }));
        return fs.getPath("/public");
    }
}
