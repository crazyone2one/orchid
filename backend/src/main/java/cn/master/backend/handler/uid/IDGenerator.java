package cn.master.backend.handler.uid;

import cn.master.backend.util.CommonBeanFactory;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public class IDGenerator {
    private static final DefaultUidGenerator DEFAULT_UID_GENERATOR;

    static {
        DEFAULT_UID_GENERATOR = CommonBeanFactory.getBean(DefaultUidGenerator.class);
    }

    /**
     * 生成一个唯一的数字
     */
    public static Long nextNum() {
        return DEFAULT_UID_GENERATOR.getUid();
    }

    /**
     * 生成一个唯一的字符串
     */
    public static String nextStr() {
        return String.valueOf(DEFAULT_UID_GENERATOR.getUid());
    }
}
