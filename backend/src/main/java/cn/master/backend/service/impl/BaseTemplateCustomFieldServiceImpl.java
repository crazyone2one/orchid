package cn.master.backend.service.impl;

import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.TemplateCustomField;
import cn.master.backend.handler.resolver.field.AbstractCustomFieldResolver;
import cn.master.backend.handler.resolver.field.CustomFieldResolverFactory;
import cn.master.backend.mapper.TemplateCustomFieldMapper;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseTemplateCustomFieldService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 模板和字段的关联关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
@Service
@RequiredArgsConstructor
public class BaseTemplateCustomFieldServiceImpl extends ServiceImpl<TemplateCustomFieldMapper, TemplateCustomField> implements BaseTemplateCustomFieldService {
    private final BaseCustomFieldService baseCustomFieldService;
    public static final ThreadLocal<Boolean> VALIDATE_DEFAULT_VALUE = new ThreadLocal<>();

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        List<String> ids = customFieldRequests.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        Set<String> fieldIdSet = baseCustomFieldService.getByIds(ids).stream().map(CustomField::getId).collect(Collectors.toSet());
        customFieldRequests = customFieldRequests.stream()
                .filter(item -> fieldIdSet.contains(item.getFieldId()))
                .toList();
        addByTemplateId(id, customFieldRequests, false);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        addByTemplateId(id, customFieldRequests, true);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByTemplateIdAndSystem(String templateId, boolean isSystem) {
        QueryChain<TemplateCustomField> queryChain = queryChain().where(TemplateCustomField::getTemplateId).eq(templateId)
                .and(TemplateCustomField::getSystemField).eq(isSystem);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public List<TemplateCustomField> getByTemplateId(String id) {
        return queryChain().where(TemplateCustomField::getTemplateId).eq(id).list();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByTemplateIds(List<String> projectTemplateIds) {
        if (projectTemplateIds.isEmpty()) {
            return;
        }
        QueryChain<TemplateCustomField> queryChain = queryChain().where(TemplateCustomField::getTemplateId).in(projectTemplateIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByTemplateId(String templateId) {
        QueryChain<TemplateCustomField> queryChain = queryChain().where(TemplateCustomField::getTemplateId).eq(templateId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public List<TemplateCustomField> getByTemplateIds(List<String> projectTemplateIds) {
        if (projectTemplateIds.isEmpty()) {
            return List.of();
        }
        return queryChain().where(TemplateCustomField::getTemplateId).in(projectTemplateIds).list();
    }

    private void addByTemplateId(String templateId, List<TemplateCustomFieldRequest> customFieldRequests, boolean isSystem) {
        AtomicReference<Integer> pos = new AtomicReference<>(0);
        List<TemplateCustomField> templateCustomFields = customFieldRequests.stream().map(field -> {
            TemplateCustomField templateCustomField = new TemplateCustomField();
            BeanUtils.copyProperties(field, templateCustomField);
            templateCustomField.setTemplateId(templateId);
            templateCustomField.setPos(pos.getAndSet(pos.get() + 1));
            templateCustomField.setDefaultValue(isSystem ? field.getDefaultValue().toString() : parseDefaultValue(field));
            templateCustomField.setSystemField(isSystem);
            return templateCustomField;
        }).toList();
        if (CollectionUtils.isNotEmpty(templateCustomFields)) {
            mapper.insertBatch(templateCustomFields);
        }
    }

    private String parseDefaultValue(TemplateCustomFieldRequest field) {
        CustomField customField = baseCustomFieldService.getWithCheck(field.getFieldId());
        AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
        customField.setRequired(false);
        if (BooleanUtils.isNotFalse(VALIDATE_DEFAULT_VALUE.get())) {
            // 创建项目时不校验默认值
            customFieldResolver.validate(customField, field.getDefaultValue());
        }
        return customFieldResolver.parse2String(field.getDefaultValue());
    }
}
