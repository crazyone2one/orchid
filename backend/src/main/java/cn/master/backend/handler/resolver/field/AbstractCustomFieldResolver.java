package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static cn.master.backend.handler.result.CommonResultCode.FIELD_VALIDATE_ERROR;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public abstract class AbstractCustomFieldResolver {
    abstract public void validate(CustomField customField, Object value);
    protected void throwValidateException(String name) {
        throw new MSException(FIELD_VALIDATE_ERROR, Translator.getWithArgs(FIELD_VALIDATE_ERROR.getMessage(), name));
    }
    protected void validateRequired(CustomField customField, Object value) {
        if (!customField.getRequired()) {
            return;
        }
        if (value == null) {
            throwValidateException(customField.getName());
        } else if (value instanceof String && StringUtils.isBlank(value.toString())) {
            throwValidateException(customField.getName());
        }
    }
    protected void validateArrayRequired(CustomField customField, Object value) {
        if (!customField.getRequired()) {
            return;
        }
        if (value == null || (value instanceof List &&  CollectionUtils.isEmpty((List) value))) {
            throwValidateException(customField.getName());
        }
    }

    protected void validateArray(String name, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof List) {
            ((List) value).forEach(v -> validateString(name, v));
        } else {
            throwValidateException(name);
        }
    }

    protected void validateString(String name, Object v) {
        if (v != null && !(v instanceof String)) {
            throwValidateException(name);
        }
    }

    public Object parse2Value(String value) {
        return value;
    }

    public String parse2String(Object value) {
        return value == null ? null : value.toString();
    }

    protected Object parse2Array(String value) {
        return value == null ? null : JSON.parseArray(value);
    }
}
