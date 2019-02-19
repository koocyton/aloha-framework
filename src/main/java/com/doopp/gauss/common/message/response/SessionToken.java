package com.doopp.gauss.common.message.response;

import lombok.Data;

@Data
public class SessionToken {

    private String token = "";

    private int expire = 7776000;

    public SessionToken(String token) {
        this.setToken(token);
    }
}
