package cn.master.backend.handler.annotation;

import cn.master.backend.constants.OperationLogType;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 操作类型
     */
    OperationLogType type() default OperationLogType.SELECT;

    /***
     * 操作函数
     */
    String expression();

    /**
     * 传入执行类
     */
    Class[] logClass() default {};
}
