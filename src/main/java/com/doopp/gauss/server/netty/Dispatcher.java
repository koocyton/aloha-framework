package com.doopp.gauss.server.netty;

import com.doopp.gauss.common.defined.CommonError;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.server.filter.iFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

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
public class Dispatcher<F> {

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
                                routes.get(requestPath, (req, resp) -> publish(req, resp, method, handleObject));
                            }
                            // POST
                            else if (method.isAnnotationPresent(POST.class)) {
                                log.info("POST " + requestPath);
                                routes.post(requestPath, (req, resp) -> publish(req, resp, method, handleObject));
                            }
                            // DELETE
                            else if (method.isAnnotationPresent(DELETE.class)) {
                                log.info("DELETE " + requestPath);
                                routes.delete(requestPath, (req, resp) -> publish(req, resp, method, handleObject));
                            }
                            // UPDATE
                            else if (method.isAnnotationPresent(PUT.class)) {
                                log.info("PUT " + requestPath);
                                routes.put(requestPath, (req, resp) -> publish(req, resp, method, handleObject));
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

    private <T> Publisher<Void> publish(HttpServerRequest req, HttpServerResponse resp, Method method, T handleObject) {

        // Filter
//        String requestPath = method.getAnnotation(Path.class).value();
//        for(iFilter filter : filters.values()) {
//            if (requestPath.contains(req.path())) {
//                if (!filter.doFilter(req, resp)) {
//                }
//            }
//        }

        // Request
        if (req.method() == HttpMethod.GET || req.method() == HttpMethod.DELETE) {
            try {
                return resp.sendString(Mono.just(
                        new GsonBuilder().create().toJson(method.invoke(handleObject, getMethodParams(method, req, null)))
                ));
            } catch (Exception e) {
                return resp.sendString(Mono.just(e.getMessage()));
            }
        }

        return resp.sendString(
                req.receive()
                        .aggregate()
                        .retain()
                        .map(byteBuf -> {
                            try {
                                return new GsonBuilder().create().toJson(method.invoke(handleObject, getMethodParams(method, req, byteBuf)));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return new GsonBuilder().create().toJson(new CommonResponse<>(
                                        new CommonException(CommonError.FAIL.code(), e.getMessage())
                                ));
                            }
                        })
        );
    }

    private Object[] getMethodParams(Method method, HttpServerRequest request, ByteBuf content) {
        ArrayList<Object> objectList = new ArrayList<>();
        Map<String, String> questParams = queryParams(request);
        Map<String, String> formParams = formParams(request, content);
        for (Parameter parameter : method.getParameters()) {
            Class<?> parameterClass = parameter.getType();
            // CookieParam
            if (parameter.getAnnotation(CookieParam.class) != null) {
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
