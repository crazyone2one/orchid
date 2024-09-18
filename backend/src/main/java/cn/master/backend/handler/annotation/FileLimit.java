package cn.master.backend.handler.annotation;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileLimit {

    /**
     * 文件大小限制 (单位: MB)
     */
    long maxSize() default 0;
}
