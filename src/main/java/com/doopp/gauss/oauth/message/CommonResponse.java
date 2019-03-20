package com.doopp.gauss.oauth.message;

import com.doopp.gauss.oauth.defined.CommonError;
import com.doopp.gauss.oauth.exception.CommonException;
import lombok.Data;

@Data
public class CommonResponse<T> {

    private int err_code = 0;

    private String err_msg = "";

    private T data;

    public CommonResponse(T data) {
        if (data instanceof CommonException) {
            CommonException _data = (CommonException) data;
            this.setErr_code(_data.getCode());
            this.setErr_msg(_data.getMessage());
        }
        else if (data instanceof Exception) {
            Exception _data = (Exception) data;
            this.setErr_code(CommonError.FAIL.code());
            this.setErr_msg(_data.getMessage());
        }
        else {
            this.data = data;
        }
    }
}
