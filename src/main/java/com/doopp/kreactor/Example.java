package com.doopp.kreactor;

import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import reactor.core.publisher.Mono;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/kreactor")
class Example {

    @GET
    @Path("/test/json")
    @Produces({MediaType.APPLICATION_JSON, "charset=UTF-8"})
    public Mono<Map<String, String>> testJson() {
        Map<String, String> map = new HashMap<>();
        map.put("hello", "hello world");
        return Mono.just(map);
    }

    @GET
    @Path("/test/html")
    @Produces({MediaType.TEXT_HTML, "charset=UTF-8"})
    public Mono<String> testHtml() {
        return Mono.just("hello world");
    }

    @GET
    @Path("/test/image")
    @Produces({"image/jpeg", "charset=UTF-8"})
    public Mono<Object> testImage() {
        return Mono.just("");
    }
}
