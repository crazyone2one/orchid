package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;
import cn.master.backend.util.JSON;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldMultipleTextResolver extends AbstractCustomFieldResolver {
    @Override
    public void validate(CustomField customField, Object value) {
        validateArrayRequired(customField, value);
        validateArray(customField.getName(), value);
    }

    @Override
    public String parse2String(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public Object parse2Value(String value) {
        return parse2Array(value);
    }
}
