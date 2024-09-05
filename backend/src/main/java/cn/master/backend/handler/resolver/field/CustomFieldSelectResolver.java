package cn.master.backend.handler.resolver.field;

import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.service.BaseCustomFieldOptionService;
import cn.master.backend.util.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public class CustomFieldSelectResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomField customField, Object value) {
        validateRequired(customField, value);
        if (value == null) {
            return;
        }
        validateString(customField.getName(), value);
        if (StringUtils.isBlank((String) value)) {
            return;
        }
        List<CustomFieldOption> options = getOptions(customField.getId());
        Set<String> values = options.stream().map(CustomFieldOption::getValue).collect(Collectors.toSet());
        if (!values.contains(value)) {
            throwValidateException(customField.getName());
        }
    }
    protected List<CustomFieldOption> getOptions(String id) {
        BaseCustomFieldOptionService customFieldOptionService = CommonBeanFactory.getBean(BaseCustomFieldOptionService.class);
        assert customFieldOptionService != null;
        return customFieldOptionService.getByFieldId(id);
    }
}
