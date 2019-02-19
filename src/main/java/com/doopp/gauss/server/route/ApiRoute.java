package com.doopp.gauss.server.route;

import com.doopp.gauss.admin.handle.LoginHandle;
import com.doopp.gauss.api.handle.OAuthHandle;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.OAuthRequest;
import com.doopp.gauss.common.message.request.LoginRequest;
import com.doopp.gauss.server.netty.AppOutbound;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRoutes;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ApiRoute {

    private AppOutbound ob;

    public ApiRoute(AppOutbound appOutbound) {
        this.ob = appOutbound;
    }

    private <T> T byteBufToObject(ByteBuf byteBuf, Type typeOfT) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = new String(bytes, StandardCharsets.UTF_8);
        return new GsonBuilder().create().fromJson(body, typeOfT);
    }

    public void setRoutes(HttpServerRoutes routes) {

        routes

//            .post("/api/login", (req, resp) ->
//                ob.sendJson(req, resp, (injector) -> {
//                    return req.receiveContent().map(bf -> {
//
//                        log.info("{}", bf);
//
//                        ByteBuf buf = bf.content();
//                        byte[] bytes = new byte[buf.readableBytes()];
//                        buf.readBytes(bytes);
//                        String body = new String(bytes, StandardCharsets.UTF_8);
//
//                        Type type = new TypeToken<OAuthRequest<LoginRequest>>(){}.getType();
//                        OAuthRequest<LoginRequest> requestObject = new GsonBuilder().create().fromJson(body, type);
//                        log.info("{}", requestObject);
//                        try {
//                            return injector.getInstance(OAuthHandle.class).login(requestObject);
//                        }
//                        catch (CommonException e) {
//                            return ob.sendJsonException(resp, e);
//                        }
//                    });
//                })
//            )
//
//            .post("/api/login", (req, resp) ->
//                    ob.sendJson(req, resp, (injector) -> {
//                        return injector.getInstance(OAuthHandle.class).login(requestParams);
//                    })
//                )
//
//                    .post("/api/register", (req, resp) ->
//                            ob.sendJson(req, resp, (injector) -> {
//                                return req.receiveContent().map(bf -> {
//
//                                    ByteBuf buf = bf.content();
//                                    byte[] bytes = new byte[buf.readableBytes()];
//                                    buf.readBytes(bytes);
//                                    String body = new String(bytes, StandardCharsets.UTF_8);
//
//                                    Type type = new TypeToken<OAuthRequest<RegisterRequest>>(){}.getType();
//                                    OAuthRequest<RegisterRequest> requestObject = new GsonBuilder().create().fromJson(body, type);
//                                    try {
//                                        return injector.getInstance(OAuthHandle.class).register(requestObject);
//                                    }
//                                    catch (CommonException e) {
//                                        return ob.sendJsonException(resp, e);
//                                    }
//                                });
//                            })
//                    )
//                    .get("/api/user", (req, resp) -> {
//                        Long id = Long.valueOf(req.param("id"));
//                        return ob.sendJson(req, resp, (injector) ->
//                                injector.getInstance(OAuthHandle.class).setUserCookie(id, req)
//                        );
//                    })

            .get("/**", ob::sendStatic);
    }
}