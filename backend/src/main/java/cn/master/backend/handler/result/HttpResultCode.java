package cn.master.backend.handler.result;

import cn.master.backend.handler.exception.IResultCode;

/**
 * 具有 Http 含义的状态码
 *
 * @author Created by 11's papa on 08/08/2024
 **/
public enum HttpResultCode implements IResultCode {
    SUCCESS(100200, "http_result_success"),
    FAILED(100500, "http_result_unknown_exception"),
    VALIDATE_FAILED(100400, "http_result_validate"),
    UNAUTHORIZED(100401, "http_result_unauthorized"),
    FORBIDDEN(100403, "http_result_forbidden"),
    NOT_FOUND(100404, "http_result_not_found");

    private final int code;
    private final String message;

    HttpResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}
