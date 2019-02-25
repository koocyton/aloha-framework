package com.doopp.gauss.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Client implements Serializable {

    // client id
    private Long id;

    // client secret
    private String secret;

    // client name
    private String name;

    private boolean allow_login;
    private boolean allow_register;

    private Date created_at;
    private Date updated_at;
}
