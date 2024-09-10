package cn.master.backend.service.impl;

import cn.master.backend.constants.CustomFieldType;
import cn.master.backend.constants.TemplateRequiredCustomField;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.entity.TemplateCustomField;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.CustomFieldMapper;
import cn.master.backend.mapper.TemplateCustomFieldMapper;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import cn.master.backend.service.BaseCustomFieldOptionService;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseOrganizationParameterService;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.backend.entity.table.TemplateCustomFieldTableDef.TEMPLATE_CUSTOM_FIELD;
import static cn.master.backend.handler.result.CommonResultCode.*;

/**
 * 自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service("baseCustomFieldService")
@RequiredArgsConstructor
public class BaseCustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField> implements BaseCustomFieldService {
    private final BaseOrganizationParameterService baseOrganizationParameterService;
    protected final BaseCustomFieldOptionService baseCustomFieldOptionService;

    private static final String CREATE_USER = "CREATE_USER";
    private final TemplateCustomFieldMapper templateCustomFieldMapper;

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

    @Override
    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
        //List<String> userIds = customFields.stream().map(CustomField::getCreateUser).toList();
        List<String> usedFieldIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customFields)) {
            usedFieldIds.addAll(QueryChain.of(TemplateCustomField.class)
                    .select(QueryMethods.distinct(TEMPLATE_CUSTOM_FIELD.FIELD_ID))
                    .from(TEMPLATE_CUSTOM_FIELD)
                    .where(TemplateCustomField::getFieldId).in(customFields.stream().map(CustomField::getId).toList())
                    .listAs(String.class));
        }
        List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(customFields.stream().map(CustomField::getId).toList());
        Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        return customFields.stream().map(item -> {
            CustomFieldDTO customFieldDTO = new CustomFieldDTO();
            BeanUtils.copyProperties(item, customFieldDTO);
            //判断有没有用到
            if (usedFieldIds.contains(item.getId())) {
                customFieldDTO.setUsed(true);
            }
            customFieldDTO.setOptions(optionMap.get(item.getId()));
            if (CustomFieldType.getHasOptionValueSet().contains(customFieldDTO.getType()) && customFieldDTO.getOptions() == null) {
                customFieldDTO.setOptions(List.of());
            }
            if (StringUtils.equalsAny(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                // 成员选项添加默认的选项
                CustomFieldOption createUserOption = new CustomFieldOption();
                createUserOption.setFieldId(item.getId());
                createUserOption.setText(Translator.get("message.domain.createUser"));
                createUserOption.setValue(CREATE_USER);
                createUserOption.setInternal(false);
                customFieldDTO.setOptions(List.of(createUserOption));
            }
            if (BooleanUtils.isTrue(item.getInternal())) {
                // 设置哪些内置字段是模板里必选的
                Set<String> templateRequiredCustomFieldSet = Arrays.stream(TemplateRequiredCustomField.values())
                        .map(TemplateRequiredCustomField::getName)
                        .collect(Collectors.toSet());
                customFieldDTO.setTemplateRequired(templateRequiredCustomFieldSet.contains(item.getName()));
                customFieldDTO.setInternalFieldKey(item.getName());
                // 翻译内置字段名称
                customFieldDTO.setName(translateInternalField(item.getName()));
            }
            return customFieldDTO;
        }).toList();
    }

    @Override
    public List<CustomField> getByScopeIdAndScene(String scopeId, String scene) {
        return queryChain()
                .where(CUSTOM_FIELD.SCOPE_ID.eq(scopeId).and(CUSTOM_FIELD.SCENE.eq(scene)))
                .list();
    }

    @Override
    public CustomFieldDTO getCustomFieldWithCheck(String id) {
        checkResourceExist(id);
        CustomField customField = mapper.selectOneById(id);
        CustomFieldDTO customFieldDTO = new CustomFieldDTO();
        BeanUtils.copyProperties(customField, customFieldDTO);
        customFieldDTO.setOptions(baseCustomFieldOptionService.getByFieldId(customFieldDTO.getId()));
        if (customField.getInternal()) {
            customFieldDTO.setInternalFieldKey(customField.getName());
            customField.setName(translateInternalField(customField.getName()));
        }
        return customFieldDTO;
    }

    @Override
    public List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options) {
        return options == null ? null : options.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            return customFieldOption;
        }).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CustomField baseAdd(CustomField customField, List<CustomFieldOption> customFieldOptions) {
        checkAddExist(customField);
        customField.setId(IDGenerator.nextStr());
        customField.setEnableOptionKey(BooleanUtils.isTrue(customField.getEnableOptionKey()));
        mapper.insert(customField);
        baseCustomFieldOptionService.addByFieldId(customField.getId(), customFieldOptions);
        return customField;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        customField.setInternal(false);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        return baseAdd(customField, customFieldOptions);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkUpdateExist(customField);
        checkResourceExist(customField.getId());
        mapper.update(customField);
        if (options != null) {
            baseCustomFieldOptionService.updateByFieldId(customField.getId(), options);
        }
        return customField;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id) {
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
        baseCustomFieldOptionService.deleteByFieldId(id);
        deleteTemplateCustomField(id);
    }

    private void deleteTemplateCustomField(String id) {
        QueryChain<TemplateCustomField> queryChain = QueryChain.of(TemplateCustomField.class).where(TEMPLATE_CUSTOM_FIELD.FIELD_ID.eq(id));
        LogicDeleteManager.execWithoutLogicDelete(() -> templateCustomFieldMapper.deleteByQuery(queryChain));
    }

    protected void checkInternal(CustomField customField) {
        if (customField.getInternal()) {
            throw new MSException(INTERNAL_CUSTOM_FIELD_PERMISSION);
        }
    }

    private void checkUpdateExist(CustomField customField) {
        if (StringUtils.isBlank(customField.getName())) {
            return;
        }
        if (queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId())
                .and(CUSTOM_FIELD.SCENE.eq(customField.getScene()))
                .and(CUSTOM_FIELD.NAME.eq(customField.getName()))
                .and(CUSTOM_FIELD.ID.ne(customField.getId()))).exists()) {
            throw new MSException(CUSTOM_FIELD_EXIST);
        }
    }

    private void checkAddExist(CustomField customField) {
        boolean exists = queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId())
                .and(CUSTOM_FIELD.SCENE.eq(customField.getScene()))
                .and(CUSTOM_FIELD.NAME.eq(customField.getName()))).exists();
        if (exists) {
            throw new MSException(CUSTOM_FIELD_EXIST);
        }
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new MSException(TEMPLATE_SCENE_ILLEGAL));
    }
}
