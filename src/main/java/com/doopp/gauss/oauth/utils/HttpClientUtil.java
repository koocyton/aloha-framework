package com.doopp.gauss.oauth.utils;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

public class HttpClientUtil {

    private HttpClient httpClient = HttpClient.create();

    public ByteBufMono get(String url, Map<String, String> headers) {
        return httpClient
            .headers(httpHeaders -> {
                for(String key : headers.keySet()) {
                    httpHeaders.set(key, headers.get(key));
                }
            })
            .get()
            .uri(url)
            .responseContent()
            .aggregate();
    }

    public Mono<ByteBuf> post(String url, Map<String, String> headers, Map<String, String> postData) {
        return httpClient
            .headers(httpHeaders -> {
                for(String key : headers.keySet()) {
                    httpHeaders.set(key, headers.get(key));
                }
            })
            .post()
            .uri(url)
            .responseContent()
            .aggregate();
    }
}
