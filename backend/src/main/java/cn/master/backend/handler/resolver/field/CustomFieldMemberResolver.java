package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldMemberResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomField customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
    }
}
