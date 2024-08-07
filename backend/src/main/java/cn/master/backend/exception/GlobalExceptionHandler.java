package cn.master.backend.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception exception) {
        exception.printStackTrace();
        return switch (exception) {
            case BadCredentialsException ignored ->
                    Map.of("code", 401, "message", exception.getMessage(), "description", "The username or password is incorrect");
            case AccountStatusException ignored ->
                    Map.of("code", 403, "message", exception.getMessage(), "description", "The account is locked");
            case AccessDeniedException ignored ->
                    Map.of("code", 403, "message", exception.getMessage(), "description", "You are not authorized to access this resource");
            case ExpiredJwtException ignored ->
                    Map.of("code", 403, "message", exception.getMessage(), "description", "The JWT token has expired");
            default ->
                    Map.of("code", 500, "message", exception.getMessage(), "description", "Unknown internal server error.");
        };
    }
}
