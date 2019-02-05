package com.doopp.gauss.server.message;

import lombok.Data;

@Data
public class CommonResponse<T> {

    private int err_code = 0;

    private String err_msg = "";

    private T data;

    public CommonResponse(T data) {
        this.data = data;
    }
}
