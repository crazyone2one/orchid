package cn.master.backend.handler.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Getter
public class MSException extends RuntimeException {
    protected IResultCode errorCode;

    public MSException(String message) {
        super(message);
    }

    public MSException(Throwable t) {
        super(t);
    }

    public MSException(IResultCode errorCode) {
        super(StringUtils.EMPTY);
        this.errorCode = errorCode;
    }

    public MSException(IResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MSException(IResultCode errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }

    public MSException(String message, Throwable t) {
        super(message, t);
    }

}
