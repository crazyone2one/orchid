package cn.master.backend.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
public class EnumValidator {
    public static <E extends Enum<E>> E validateEnum(Class<E> enumClass, String value) {
        if (StringUtils.isBlank(value)) {
            LogUtils.error("Invalid value for enum " + enumClass.getSimpleName() + ": " + value);
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            LogUtils.error("Invalid value for enum " + enumClass.getSimpleName() + ": " + value, e);
            return null;
        }
    }
}
