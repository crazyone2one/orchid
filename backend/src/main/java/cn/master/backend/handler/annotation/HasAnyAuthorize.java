package cn.master.backend.handler.annotation;

import cn.master.backend.constants.Logical;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasAnyAuthorize {
    String[] value();

    Logical logical() default Logical.AND;
}
