package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldFloatResolver extends AbstractCustomFieldResolver {
    @Override
    public void validate(CustomField customField, Object value) {
        validateRequired(customField, value);
        if (value != null && !(value instanceof Number)) {
            throwValidateException(customField.getName());
        }
    }

    @Override
    public Object parse2Value(String value) {
        return value == null ? null : Float.parseFloat(value);
    }
}
