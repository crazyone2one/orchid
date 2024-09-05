package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;
import cn.master.backend.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldDateTimeResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomField customField, Object value) {
        validateRequired(customField, value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateUtils.getTime(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }
}
