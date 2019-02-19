package com.doopp.gauss.common.message.request;

import lombok.Data;

@Data
public class RegisterRequest {

    private String account;

    private String password;

    private String repeatPassword;
}
