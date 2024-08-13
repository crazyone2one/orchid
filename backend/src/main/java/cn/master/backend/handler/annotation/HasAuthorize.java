package cn.master.backend.handler.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 08/09/2024
 **/
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@auth.hasAuthority('{value}')")
public @interface HasAuthorize {
    String value();
}
