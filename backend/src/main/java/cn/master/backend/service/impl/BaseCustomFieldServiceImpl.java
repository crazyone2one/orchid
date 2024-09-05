package cn.master.backend.service.impl;

import cn.master.backend.entity.CustomField;
import cn.master.backend.mapper.CustomFieldMapper;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseOrganizationParameterService;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
@RequiredArgsConstructor
public class BaseCustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField> implements BaseCustomFieldService {
    private final BaseOrganizationParameterService baseOrganizationParameterService;

    @Override
    public List<CustomField> getByIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return List.of();
        }
        return queryChain().where(CustomField::getId).in(fieldIds).list();
    }

    @Override
    public CustomField getWithCheck(String id) {
        checkResourceExist(id);
        return mapper.selectOneById(id);
    }

    @Override
    public CustomField checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_custom_field.name");
    }

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        String key = baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene);
        String value = baseOrganizationParameterService.getValue(orgId, key);
        // 没有配置默认为 true
        return !StringUtils.equals(BooleanUtils.toStringTrueFalse(false), value);
    }

    @Override
    public List<CustomField> getByRefIdsAndScopeId(List<String> fieldIds, String scopeId) {
        if (fieldIds.isEmpty()) {
            return List.of();
        }
        return queryChain().where(CustomField::getRefId).in(fieldIds)
                .and(CustomField::getScopeId).eq(scopeId).list();
    }

    @Override
    public String translateInternalField(String filedName) {
        return Translator.get("custom_field." + filedName);
    }
}
