package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import com.doopp.gauss.server.filter.AppFilter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class AppRoutes {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    @Inject
    private HelloHandle helloHandle;

    @Inject
    private Gson gson;

    @Inject
    private AppFilter appFilter;

    @Inject
    private Injector injector;

    public Consumer<HttpServerRoutes> getRoutesConsumer() {

        return routes -> routes
                .get("/user/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return sendJson(req, resp, helloHandle.hello(id));
                })
                .ws("/game", (in, out) -> out.send(
                    helloHandle.game(in.receive())
                ))
                .get("/**", this::sendStaticFile);
    }

    private NettyOutbound sendStaticFile(HttpServerRequest req, HttpServerResponse resp) {
        if (!appFilter.doFilter(req, resp, injector)) {
            return appFilter.sendFilterException(resp);
        }
        String requestUri = (req.uri().equals("/") || req.uri().equals("")) ? "/index.html" : req.uri();
        URL fileUrl = AppRoutes.class.getResource("/public" + requestUri);
        if (fileUrl==null) {
            return resp.status(HttpResponseStatus.NOT_FOUND);
        }
        if (!fileUrl.toString().contains(".jar!")) {
            try {
                Path filePath = Paths.get(fileUrl.toURI());
                return resp.sendFile(filePath);
            }
            catch(URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        try (InputStream fileIs = AppRoutes.class.getResourceAsStream("/public" + requestUri)) {
            if (fileIs==null) {
                return resp.status(HttpResponseStatus.NOT_FOUND);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bs = new byte[1024];
            int len;
            while ((len = fileIs.read(bs)) != -1) {
                bout.write(bs, 0, len);
            }
            ByteBuf buf = Unpooled.wrappedBuffer(bout.toByteArray()).retain();
            return resp.send(ByteBufMono.just(buf));
        }
        catch(IOException ue) {
            throw new RuntimeException(ue);
        }
    }

    private <T> NettyOutbound sendJson(HttpServerRequest req, HttpServerResponse resp, T handle) {
        if (!appFilter.doFilter(req, resp, injector)) {
            return appFilter.sendFilterException(resp);
        }
        Mono<String> monoJson = Mono.just(gson.toJson(handle));
        return resp
                .status(HttpResponseStatus.OK)
                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .sendString(monoJson);
    }
}
