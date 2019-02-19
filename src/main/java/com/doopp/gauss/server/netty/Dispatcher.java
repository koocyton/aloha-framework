package com.doopp.gauss.server.netty;

import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.message.OAuthRequest;
import com.doopp.gauss.common.message.request.LoginRequest;
import com.doopp.gauss.server.filter.iFilter;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.VFS;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerRoutes;

import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@Slf4j
public class Dispatcher {

    private Injector injector;

    private Map<String, iFilter> filters = new HashMap<>();

    private Set<String> handlePackages = new HashSet<>();

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
                    // methods for handle
                    Method[] handleMethods = handleObject.getClass().getMethods();
                    // loop methods
                    for (Method method : handleMethods) {
                        // if have request path
                        if (method.isAnnotationPresent(Path.class)) {
                            String requestPath = method.getAnnotation(Path.class).value();
                            // GET
                            if (method.isAnnotationPresent(GET.class)) {
                                log.info("GET " + requestPath);
                                routes.get(requestPath, (req, resp) -> {
                                    try {
                                         return resp.sendString(Mono.just(
                                            new GsonBuilder().create().toJson(method.invoke(handleObject, getMethodParams(method, req, null)))
                                        ));
                                    } catch (Exception e) {
                                        return resp.sendString(Mono.just(e.getMessage()));
                                    }
                                });
                            }
                            // POST
                            else if (method.isAnnotationPresent(POST.class)) {
                                log.info("POST " + requestPath);
                                routes.post(requestPath, (req, resp) -> {
                                    // return resp.sendString(Mono.just("aaa"));
                                    return resp.sendString(
                                            req.receiveContent()
                                                    .map(httpContent -> {
                                                        //ByteBuf bf = httpContent.content();
                                                        //byte[] bytes = new byte[bf.readableBytes()];
                                                        //bf.readBytes(bytes);
                                                        //String body = new String(bytes, StandardCharsets.UTF_8);
                                                        //log.info("body {}", body);
                                                        getRequestParams(req, httpContent);
                                                        //Type type = new TypeToken<OAuthRequest<LoginRequest>>() {}.getType();
                                                        //OAuthRequest<LoginRequest> requestObject = new GsonBuilder().create().fromJson(body, type);
                                                        try {
                                                            return new GsonBuilder().create().toJson(null);
                                                            // return new GsonBuilder().create().toJson(method.invoke(handleObject, getMethodParams(method, req, httpContent)));
                                                        } catch (Exception e) {
                                                            return new GsonBuilder().create().toJson(e);
                                                        }
                                                    }));
                                });
                            }
                            // DELETE
                            else if (method.isAnnotationPresent(DELETE.class)) {
                                log.info("DELETE " + requestPath);
                                routes.delete(requestPath, (req, resp) -> resp.sendString(Mono.just(requestPath)));
                            }
                            // UPDATE
                            else if (method.isAnnotationPresent(PUT.class)) {
                                log.info("PUT " + requestPath);
                                routes.put(requestPath, (req, resp) -> resp.sendString(Mono.just(requestPath)));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private Object[] getMethodParams(Method method, HttpServerRequest request, HttpContent httpContent) {
        ArrayList<Object> objectList = new ArrayList<>();
        Map<String, String> questParams = getRequestParams(request, httpContent);
        log.info("{}", questParams);
        for (Parameter parameter : method.getParameters()) {
            Class parameterClass;
            try {
                parameterClass = Class.forName(parameter.getType().getTypeName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException();
            }
            // QueryParam
            if (parameter.getAnnotation(QueryParam.class) != null) {
                String requestParamKey = parameter.getAnnotation(QueryParam.class).value();
                objectList.add(getParamTypeValue(request.param(requestParamKey), parameterClass));
            }
            // PathParam
            else if (parameter.getAnnotation(PathParam.class) != null) {
                String requestParamKey = parameter.getAnnotation(PathParam.class).value();
                objectList.add(getParamTypeValue(request.param(requestParamKey), parameterClass));
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
        } else {
            return clazz.cast(value);
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

    // 处理 Get Post 请求
    private Map<String, String> getRequestParams(HttpServerRequest request, HttpContent httpContent) {
        Map<String, String> requestParams = new HashMap<>();
        // Query Params
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        for (Map.Entry<String, List<String>> next : params.entrySet()) {
            requestParams.put(next.getKey(), next.getValue().get(0));
        }
        // POST Params
        ByteBuf byteBuf = httpContent.content();
        byte[] byteArray = new byte[byteBuf.capacity()];
        byteBuf.readBytes(byteArray);
        DefaultHttpRequest dhr = new DefaultFullHttpRequest(request.version(), request.method(), request.uri(), Unpooled.wrappedBuffer(byteArray));
        log.info(" bb {}", byteBufToString(httpContent.content()));
        log.info("dhr {}", dhr);
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), dhr);
        List<InterfaceHttpData> postData = postDecoder.getBodyHttpDatas();
        log.info("postData {}", postData);
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                requestParams.put(attribute.getName(), attribute.getValue());
            }
        }
        return requestParams;
    }

    private String byteBufToString(ByteBuf byteBuf) {
        byte[] byteArray = new byte[byteBuf.capacity()];
        byteBuf.readBytes(byteArray);
        return new String(byteArray);
    }

//    public synchronized List<InterfaceHttpData> getMultipartParts() {
//        if (!isMultipartRequest() || !isCompleteRequestWithAllChunks())
//            return null;
//        if (multipartData == null) {
//            byte[] contentBytes = getRawContentBytes();
//            HttpRequest fullHttpRequestForMultipartDecoder = (contentBytes == null)
//                    ? new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri())
//                    : new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri(), Unpooled.wrappedBuffer(contentBytes));
//            fullHttpRequestForMultipartDecoder.headers().add(getHeaders());
//            multipartData = new HttpPostMultipartRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequestForMultipartDecoder, getContentCharset());
//        }
//        return multipartData.getBodyHttpDatas();
//    }
}
