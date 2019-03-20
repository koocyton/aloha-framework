package com.doopp.gauss.oauth.defined;

public enum CommonError {

    SUCCESS             (  1, ""),
    ACCOUNT_NO_EXIST    (401,"account not exist"),
    FAIL                (500,""),
    PASSWORD_INCORRECT  (501,"password incorrect"),
    MANAGER_NO_LOGIN    (502,"manager not login"),
    WRONG_SESSION       (503,"wrong session"),
    EXPIRE_TIME         (504,"expire time"),
    CLIENT_FAILED       (505,"client is failed"),
    CLIENT_NO_EXIST     (506,"client not exist"),
    PASSWORD_IS_SHORT   (507,"password too short"),
    DIFFERENT_PASSWORD  (508,"two inconsistent passwords"),
    REJECT_LOGIN        (509,"reject login"),
    REJECT_REGISTER     (510,"reject register"),
    UNSAFE_REQUEST      (511,"unsafe request"),
    ACCOUNT_EXIST       (512,"account already exists");

    private int code;

    private String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String message() {
        return this.message;
    }

    public int code() {
        return this.code;
    }
}
