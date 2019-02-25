package com.doopp.gauss.common.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserVO implements Serializable {

    // 主键
    private Long id;

    // 用户名
    private String name;
}
