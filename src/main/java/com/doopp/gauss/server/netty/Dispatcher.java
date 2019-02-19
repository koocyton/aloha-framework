package com.doopp.gauss.server.netty;

import com.doopp.gauss.server.filter.iFilter;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
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
import java.net.URL;
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
                                            new GsonBuilder().create().toJson(method.invoke(handleObject, getMethodParams(method, req)))
                                        ));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return resp.sendString(Mono.just("error"));
                                });
                            }
                            // POST
                            else if (method.isAnnotationPresent(POST.class)) {
                                log.info("POST " + requestPath);
                                routes.post(requestPath, (req, resp) -> resp.sendString(Mono.just(requestPath)));
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private Object[] getMethodParams(Method method, HttpServerRequest request) {
        log.info("11 {}", request.receiveContent().map(bf->{
            return "abc";
        }).toString());
        ArrayList<Object> objectList = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            Class parameterClass;
            try {
                parameterClass = Class.forName(parameter.getType().getTypeName());
            }
            catch (ClassNotFoundException e) {
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
        }
        else if (clazz == Integer.class) {
            return clazz.cast(Integer.valueOf(value));
        }
        else if (clazz == Boolean.class) {
            return clazz.cast(Boolean.valueOf(value));
        }
        else {
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
                for(File file : files) {
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
    private Map<String, String> getRequestParams(HttpRequest req) {
        Map<String, String> requestParams = new HashMap<>();
        // 处理get请求
        if (req.method() == HttpMethod.GET || req.method() == HttpMethod.POST) {
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> params = decoder.parameters();
            for (Map.Entry<String, List<String>> next : params.entrySet()) {
                requestParams.put(next.getKey(), next.getValue().get(0));
            }
        }
        // 处理POST请求
        if (req.method() == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
            List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
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
