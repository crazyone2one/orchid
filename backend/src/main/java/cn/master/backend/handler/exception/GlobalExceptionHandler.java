package cn.master.backend.handler.exception;

import cn.master.backend.handler.result.HttpResultCode;
import cn.master.backend.handler.result.ResultHolder;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultHolder handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResultHolder.error(HttpResultCode.VALIDATE_FAILED.getCode(), HttpResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResultHolder> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultHolder.error(HttpResultCode.NOT_FOUND.getCode(), HttpResultCode.NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(MSException.class)
    public ResponseEntity<ResultHolder> handlerMSException(MSException e) {
        IResultCode errorCode = e.getErrorCode();
        if (errorCode == null) {
            // 如果抛出异常没有设置状态码，则返回错误 message
            return ResponseEntity.internalServerError()
                    .body(ResultHolder.error(HttpResultCode.FAILED.getCode(), e.getMessage()));
        }

        int code = errorCode.getCode();
        String message = errorCode.getMessage();
        message = Translator.get(message, message);

        if (errorCode instanceof HttpResultCode) {
            // 如果是 MsHttpResultCode，则设置响应的状态码，取状态码的后三位
            if (errorCode.equals(HttpResultCode.NOT_FOUND)) {
                message = getNotFoundMessage(message);
            }
            return ResponseEntity.status(code % 1000)
                    .body(ResultHolder.error(code, message, e.getMessage()));
        } else {
            // 响应码返回 500，设置业务状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultHolder.error(code, Translator.get(message, message), e.getMessage()));
        }
    }

    private String getNotFoundMessage(String message) {
        String resourceName = ServiceUtils.getResourceName();
        if (StringUtils.isNotBlank(resourceName)) {
            message = String.format(message, Translator.get(resourceName, resourceName));
        } else {
            message = String.format(message, Translator.get("resource.name"));
        }
        ServiceUtils.clearResourceName();
        return message;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) {
        getStackTraceAsString(exception);
        return switch (exception) {
            case BadCredentialsException ignored -> ResponseEntity.status(401)
                    .body(ResultHolder.error(HttpResultCode.UNAUTHORIZED.getCode(), Translator.get(HttpResultCode.UNAUTHORIZED.getMessage()), exception.getMessage()));
            case AccountStatusException ignored -> ResponseEntity.status(403)
                    .body(Map.of("code", 403, "message", exception.getMessage(), "description", "The account is locked"));
            case AccessDeniedException ignored -> ResponseEntity.status(403)
                    //.body(Map.of("code", 403, "message", exception.getMessage(), "description", "You are not authorized to access this resource"));
                    .body(ResultHolder.error(HttpResultCode.FORBIDDEN.getCode(), exception.getMessage(), "You are not authorized to access this resource"));
            case ExpiredJwtException ignored -> ResponseEntity.status(401)
                    //.body(Map.of("code", 401, "message", exception.getMessage(), "description", "The JWT token has expired"));
                    .body(ResultHolder.error(HttpResultCode.UNAUTHORIZED.getCode(), exception.getMessage(), "The JWT token has expired"));
            default -> ResponseEntity.status(500)
                    .body(ResultHolder.error(HttpResultCode.FAILED.getCode(),
                            exception.getMessage(), getStackTraceAsString(exception)));
        };
    }

    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
