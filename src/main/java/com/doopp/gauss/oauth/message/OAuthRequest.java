package com.doopp.gauss.oauth.message;

import lombok.Data;

@Data
public class OAuthRequest<T> {

    private int time;

    private Long client;

    private String security;

    private T data;

    public OAuthRequest(T data) {
        this.data = data ;
    }
}
