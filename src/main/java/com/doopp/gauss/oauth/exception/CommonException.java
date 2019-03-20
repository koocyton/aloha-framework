package com.doopp.gauss.oauth.exception;

import com.doopp.gauss.oauth.defined.CommonError;

public class CommonException extends Exception {

    private int errorCode = 0;

    public CommonException(String errorMessage) {
        super(errorMessage);
        this.errorCode = CommonError.FAIL.code();
    }

    public CommonException(CommonError commonError) {
        super(commonError.message());
        this.errorCode = commonError.code();
    }

    public CommonException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public int getCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return super.getMessage();
    }
}
