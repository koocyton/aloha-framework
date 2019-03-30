package com.doopp.kreactor;


public class KReactorException extends Exception {

    private int errorCode = 0;

    public KReactorException(int errorCode, String errorMessage) {
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
