package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;
import cn.master.backend.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldDateResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomField customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateUtils.getDate(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }
}
