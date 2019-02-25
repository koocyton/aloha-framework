package com.doopp.gauss.common.entity;

import com.doopp.gauss.common.utils.EncryHelper;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {

    // 主键
    private Long id;

    // 用户名
    private String name;

    // 平台
    // private String platform = "";

    // 平台用户 ID
    // private String platform_id = "";

    // 平台用户 token
    // private String platform_token = "";

    // 账号
    private String account;

    // 密码
    private String password;

    // 创建时间
    private Date created_at;

    // 加密密码
    public String getHashPassword(String password) {
        return EncryHelper.md5(this.account + " " + password);
    }
}
