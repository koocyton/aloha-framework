package com.doopp.gauss.oauth.message.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String account;

    private String password;
}
