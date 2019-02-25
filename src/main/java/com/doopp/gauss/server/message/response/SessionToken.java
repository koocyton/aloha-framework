package com.doopp.gauss.server.message.response;

public class SessionToken {

    private String token = "";

    private int expire = 7776000;

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public SessionToken(String token) {
        this.setToken(token);
    }
}
