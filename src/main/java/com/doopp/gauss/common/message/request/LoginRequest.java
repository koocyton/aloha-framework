package com.doopp.gauss.common.message.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String account;

    private String password;
}
