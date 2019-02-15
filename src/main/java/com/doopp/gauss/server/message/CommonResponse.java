package com.doopp.gauss.server.message;

public class CommonResponse<T> {

    private int err_code = 0;

    private String err_msg = "";

    private T data;

    public void setErr_code(int err_code) {
        this.err_code = err_code;
    }

    public void setErr_msg(String err_msg) {
        this.err_msg = err_msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public CommonResponse(T data) {
        this.data = data;
    }
}
