package com.doopp.gauss.common.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ClientVO implements Serializable {

    // client id
    private String id;

    // client secret
    private String secret;

    // client name
    private String name;

    private boolean allow_login;
    private boolean allow_register;

    private Date created_at;
    private Date updated_at;
}
