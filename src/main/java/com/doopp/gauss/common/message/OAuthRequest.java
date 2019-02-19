package com.doopp.gauss.common.message;

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
