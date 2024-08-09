package cn.master.backend.handler.exception;

import cn.master.backend.util.Translator;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface IResultCode {
    /**
     * 返回状态码
     */
    int getCode();

    /**
     * 返回状态码信息
     */
    String getMessage();

    /**
     * 返回国际化后的状态码信息
     * 如果没有匹配则返回原文
     */
    default String getTranslationMessage(String message) {
        return Translator.get(message, message);
    }
}
