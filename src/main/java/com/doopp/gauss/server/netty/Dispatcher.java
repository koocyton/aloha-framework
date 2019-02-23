package com.doopp.gauss.server.netty;

import com.doopp.gauss.common.defined.CommonError;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.server.filter.iFilter;
import com.doopp.gauss.server.handle.StaticHandle;
import com.doopp.gauss.server.handle.WebSocketServerHandle;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyPipeline;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class Dispatcher {

    private Injector injector;

    private final Map<String, iFilter> filters = new HashMap<>();

    private final Set<String> handlePackages = new HashSet<>();

    private final Map<String, Channel> wsChannels = new HashMap<>();

    private final Gson gsonCreate = new GsonBuilder()
			.serializeNulls()
			.setDateFormat("yyyy-MM-dd HH:mm:ss")
			.setLongSerializationPolicy(LongSerializationPolicy.STRING)
			.create();

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void setHandlePackages(String... packages) {
        Collections.addAll(this.handlePackages, packages);
    }

    public void addFilter(String path, iFilter filter) {
        this.filters.put(path, filter);
    }

    public Consumer<HttpServerRoutes> setHandleMethodRoute() {
        return routes -> {
            try {
                Set<String> handleClassesName = this.getHandleClassesName();
                for (String handleClassName : handleClassesName) {
                    // handle
                    Object handleObject = injector.getInstance(Class.forName(handleClassName));
                    Path pathAnnotation = handleObject.getClass().getAnnotation(Path.class);
                    String rootPath = (pathAnnotation == null) ? "" : pathAnnotation.value();
                    // if websocket
                    if (handleObject instanceof WebSocketServerHandle && pathAnnotation != null) {
                        log.info("    WS " + rootPath + " → " + handleClassName);
                        routes.ws(rootPath, (in, out) -> websocketPublisher(in, out, handleObject));
                        continue;
                    }
                    // methods for handle
                    Method[] handleMethods = handleObject.getClass().getMethods();
                    // loop methods
                    for (Method method : handleMethods) {
                        // if have request path
                        if (method.isAnnotationPresent(Path.class)) {
                            String requestPath = rootPath + method.getAnnotation(Path.class).value();
                            // GET
                            if (method.isAnnotationPresent(GET.class)) {
                                log.info("   GET " + requestPath + " → " + handleClassName + ":" + method.getName());
                                routes.get(requestPath, (req, resp) -> httpGetPublisher(req, resp, method, handleObject));
                            }
                            // POST
                            else if (method.isAnnotationPresent(POST.class)) {
                                log.info("  POST " + requestPath + " → " + handleClassName + ":" + method.getName());
                                routes.post(requestPath, (req, resp) -> httpPostPublisher(req, resp, method, handleObject));
                            }
                            // DELETE
                            else if (method.isAnnotationPresent(DELETE.class)) {
                                log.info("DELETE " + requestPath + " → " + handleClassName + ":" + method.getName());
                                routes.delete(requestPath, (req, resp) -> httpGetPublisher(req, resp, method, handleObject));
                            }
                            // UPDATE
                            else if (method.isAnnotationPresent(PUT.class)) {
                                log.info("   PUT " + requestPath + " → " + handleClassName + ":" + method.getName());
                                routes.put(requestPath, (req, resp) -> httpPostPublisher(req, resp, method, handleObject));
                            }
                        }
                    }
                }
                routes.get("/**", (new StaticHandle())::sendStatic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private <T> Publisher<Void> websocketPublisher(WebsocketInbound in, WebsocketOutbound out, T handleObject) {
        // cast websocket handle
        WebSocketServerHandle webSocketServerHandle = (WebSocketServerHandle) handleObject;
        // with connect
        return out.withConnection(c -> {
            // on connect
            // wsChannels.put(in.headers().get("Sec-WebSocket-Key"), c.channel());
            webSocketServerHandle.onConnect(c.channel());
            // get message
            in.aggregateFrames()
                 .receiveFrames()
                 .map(frame -> {
                     if (frame instanceof TextWebSocketFrame) {
                         webSocketServerHandle.onTextMessage((TextWebSocketFrame) frame, c.channel());
                     } else if (frame instanceof BinaryWebSocketFrame) {
                         webSocketServerHandle.onBinaryMessage((BinaryWebSocketFrame) frame, c.channel());
                     } else if (frame instanceof PingWebSocketFrame) {
                         webSocketServerHandle.onPingMessage((PingWebSocketFrame) frame, c.channel());
                     } else if (frame instanceof PongWebSocketFrame) {
                         webSocketServerHandle.onPongMessage((PongWebSocketFrame) frame, c.channel());
                     } else if (frame instanceof CloseWebSocketFrame) {
                         webSocketServerHandle.close((CloseWebSocketFrame) frame, c.channel());
                     }
                     return null;
                 })
                 // .map(TextWebSocketFrame::new)
                 .blockLast();
        });
        // .options(NettyPipeline.SendOptions::flushOnEach)
        // .sendObject(in
        //         .aggregateFrames()
        //         .receiveFrames()
        //         .map(frame -> {
        //             String wsKey = in.headers().get("Sec-WebSocket-Key");
        //             if (frame instanceof TextWebSocketFrame) {
        //                 webSocketServerHandle.onTextMessage((TextWebSocketFrame)frame, wsChannels.get(wsKey));
        //             }
        //             else if (frame instanceof BinaryWebSocketFrame) {
        //                 webSocketServerHandle.onBinaryMessage(handles.get(handleKey));
        //             }
        //             else if (frame instanceof PingWebSocketFrame) {
        //                 webSocketServerHandle.onPingMessage(handles.get(handleKey));
        //             }
        //             else if (frame instanceof PongWebSocketFrame) {
        //                 webSocketServerHandle.onPongMessage(handles.get(handleKey));
        //             }
        //             else if (frame instanceof CloseWebSocketFrame) {
        //                 webSocketServerHandle.close(handles.get(handleKey));
        //                 handles.remove(handleKey);
        //             }
        //             return "";
        //         })
        //         .map(TextWebSocketFrame::new)
        // );
    }

    private <T> Publisher<Void> httpGetPublisher(HttpServerRequest req, HttpServerResponse resp, Method method, T handleObject) {
        return this.httpPublisher(req, resp, method, handleObject, null);
    }

    private <T> Publisher<Void> httpPostPublisher(HttpServerRequest req, HttpServerResponse resp, Method method, T handleObject) {
        return resp.addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .status(HttpResponseStatus.OK)
            .sendString(req
                .receive()
                .map(byteBuf -> {
                    CommonResponse<Object> response;
                    try {
                        response = (CommonResponse<Object>) method.invoke(handleObject, getMethodParams(method, req, resp, byteBuf));
                    } catch (Exception e) {
                        response = exceptionPublisher(e);
                    }
                    return response;
                })
                .map(gsonCreate::toJson)
            );
    }

    private <T> Publisher<Void> httpPublisher(HttpServerRequest req, HttpServerResponse resp, Method method, T handleObject, ByteBuf content) {
        CommonResponse<Object> response;
        int status = HttpResponseStatus.OK.code();
        try {
            response = (CommonResponse<Object>) method.invoke(handleObject, getMethodParams(method, req, resp, content));
        } catch (Exception e) {
            response = exceptionPublisher(e);
            status = response.getErr_code();
        }
        return resp.addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .status(status)
            .sendString(Mono.just(response).map(gsonCreate::toJson));
    }

    private CommonResponse<Object> exceptionPublisher(Exception e) {
        // default error info
        int errorCode = CommonError.FAIL.code();
        String errorMessage = e.getMessage();
        // if is CommonException
        if (e.getCause()!=null && e.getCause().getClass()==CommonException.class) {
            CommonException ce = (CommonException) e.getCause();
            errorCode = ce.getCode();
            errorMessage = ce.getMessage();
        }
        // return;
        CommonResponse<Object> responseObject = new CommonResponse<>(null);
        responseObject.setErr_code(errorCode);
        responseObject.setErr_msg(errorMessage);
        return responseObject;
    }

    private Object[] getMethodParams(Method method, HttpServerRequest request, HttpServerResponse response, ByteBuf content) {
        ArrayList<Object> objectList = new ArrayList<>();
        Map<String, String> questParams = queryParams(request);
        Map<String, String> formParams = formParams(request, content);
        for (Parameter parameter : method.getParameters()) {
            Class<?> parameterClass = parameter.getType();
            // RequestAttribute
            if (parameterClass == RequestAttribute.class) {
                objectList.add(new RequestAttribute());
            }
            // request
            else if (parameterClass == HttpServerRequest.class) {
                objectList.add(request);
            }
            // response
            else if (parameterClass == HttpServerResponse.class) {
                objectList.add(response);
            }
            // CookieParam
            else if (parameter.getAnnotation(CookieParam.class) != null) {
                String annotationKey = parameter.getAnnotation(CookieParam.class).value();
                objectList.add(getParamTypeValue(request.cookies().get(annotationKey).toString(), parameterClass));
            }
            // HeaderParam
            else if (parameter.getAnnotation(HeaderParam.class) != null) {
                String annotationKey = parameter.getAnnotation(HeaderParam.class).value();
                objectList.add(getParamTypeValue(request.requestHeaders().get(annotationKey), parameterClass));
            }
            // QueryParam
            else if (parameter.getAnnotation(QueryParam.class) != null) {
                String annotationKey = parameter.getAnnotation(QueryParam.class).value();
                objectList.add(getParamTypeValue(questParams.get(annotationKey), parameterClass));
            }
            // PathParam
            else if (parameter.getAnnotation(PathParam.class) != null) {
                String annotationKey = parameter.getAnnotation(PathParam.class).value();
                objectList.add(getParamTypeValue(request.param(annotationKey), parameterClass));
            }
            // FormParam
            else if (parameter.getAnnotation(FormParam.class) != null) {
                String annotationKey = parameter.getAnnotation(FormParam.class).value();
                objectList.add(getParamTypeValue(formParams.get(annotationKey), parameterClass));
            }
            // BeanParam
            else if (parameter.getAnnotation(BeanParam.class) != null) {
                byte[] byteArray = new byte[content.capacity()];
                content.readBytes(byteArray);
                // Type type = TypeToken.get(parameter.getAnnotatedType().getType()).getType();
                objectList.add((new Gson()).fromJson(new String(byteArray), parameter.getAnnotatedType().getType()));
            }
            // default
            else {
                objectList.add(parameterClass.cast(null));
            }
        }
        return objectList.toArray();
    }

    private <T> T getParamTypeValue(String value, Class<T> clazz) {
        if (clazz == Long.class) {
            return clazz.cast(Long.valueOf(value));
        } else if (clazz == Integer.class) {
            return clazz.cast(Integer.valueOf(value));
        } else if (clazz == Boolean.class) {
            return clazz.cast(Boolean.valueOf(value));
        } else if (clazz == String.class) {
            return clazz.cast(value);
        } else {
            return new GsonBuilder().create().fromJson(value, clazz);
        }
    }

    private Set<String> getHandleClassesName() throws IOException {
        // init result
        Set<String> handleClassesName = new HashSet<>();
        // loop search handle
        for (String packageName : this.handlePackages) {
            String path = "/" + packageName.replace(".", "/");
            String resourcePath = this.getClass().getResource(path).getPath();
            // if in jar
            if (resourcePath.contains(".jar")) {
                JarFile jarFile = new JarFile(resourcePath.substring(6, resourcePath.lastIndexOf(".jar!") + 4));
                Enumeration<JarEntry> entrys = jarFile.entries();
                while (entrys.hasMoreElements()) {
                    JarEntry jar = entrys.nextElement();
                    String name = jar.getName();
                    if (name.contains(packageName.replace(".", "/")) && name.contains(".class")) {
                        int beginIndex = packageName.length() + 1;
                        int endIndex = name.lastIndexOf(".class");
                        String className = name.substring(beginIndex, endIndex);
                        handleClassesName.add(packageName + "." + className);
                    }
                }
            }
            // if not jar
            else {
                File dir = new File(resourcePath);
                File[] files = dir.listFiles();
                for (File file : files) {
                    String name = file.getName();
                    int endIndex = name.lastIndexOf(".class");
                    String className = name.substring(0, endIndex);
                    handleClassesName.add(packageName + "." + className);
                }
            }
        }
        return handleClassesName;
    }

    // Get 请求
    private Map<String, String> queryParams(HttpServerRequest request) {
        Map<String, String> requestParams = new HashMap<>();
        // Query Params
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        for (Map.Entry<String, List<String>> next : params.entrySet()) {
            requestParams.put(next.getKey(), next.getValue().get(0));
        }
        return requestParams;
    }

    // 简单的 Post 请求
    private Map<String, String> formParams(HttpServerRequest request, ByteBuf content) {
        Map<String, String> requestParams = new HashMap<>();
        if (content != null) {
            // POST Params
            FullHttpRequest dhr = new DefaultFullHttpRequest(request.version(), request.method(), request.uri(), content);
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), dhr);
            List<InterfaceHttpData> postData = postDecoder.getBodyHttpDatas();
            for (InterfaceHttpData data : postData) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    MemoryAttribute attribute = (MemoryAttribute) data;
                    requestParams.put(attribute.getName(), attribute.getValue());
                }
            }
        }
        return requestParams;
    }
}
