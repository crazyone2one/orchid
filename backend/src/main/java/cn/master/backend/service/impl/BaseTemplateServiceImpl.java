package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScene;
import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.entity.Template;
import cn.master.backend.entity.TemplateCustomField;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.resolver.field.AbstractCustomFieldResolver;
import cn.master.backend.handler.resolver.field.CustomFieldResolverFactory;
import cn.master.backend.mapper.TemplateMapper;
import cn.master.backend.payload.dto.system.template.TemplateCustomFieldDTO;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import cn.master.backend.service.BaseCustomFieldOptionService;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseTemplateCustomFieldService;
import cn.master.backend.service.BaseTemplateService;
import cn.master.backend.util.LogUtils;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.TemplateTableDef.TEMPLATE;
import static cn.master.backend.handler.result.CommonResultCode.*;

/**
 * 模版 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
@Service("baseTemplateService")
@RequiredArgsConstructor
public class BaseTemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements BaseTemplateService {
    protected final BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    protected final BaseCustomFieldService baseCustomFieldService;
    protected final BaseCustomFieldOptionService baseCustomFieldOptionService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkAddExist(template);
        mapper.insert(template);
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        return template;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkResourceExist(template.getId());
        checkUpdateExist(template);
        if (customFields != null) {
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), false);
            baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        }
        if (systemFields != null) {
            // 系统字段
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), true);
            baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        }
        mapper.update(template);
        return template;
    }

    private void checkUpdateExist(Template template) {
        if (StringUtils.isBlank(template.getName())) {
            return;
        }
        boolean exists = queryChain()
                .where(TEMPLATE.SCOPE_ID.eq(template.getScopeId())
                        .and(TEMPLATE.NAME.eq(template.getName()))
                        .and(TEMPLATE.ID.ne(template.getId()))
                        .and(TEMPLATE.SCENE.eq(template.getScene())))
                .exists();
        if (exists) {
            throw new MSException(TEMPLATE_EXIST);
        }
    }

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        return baseCustomFieldService.isOrganizationTemplateEnable(orgId, scene);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        template.setInternal(false);
        return baseAdd(template, customFields, systemFields);
    }

    @Override
    public List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields) {
        if (customFields == null) {
            return List.of();
        }
        List<String> fieldIds = customFields.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        // 查询当前组织字段所对应的项目字段，构建map，键为组织字段ID，值为项目字段ID
        Map<String, String> refFieldMap = baseCustomFieldService.getByRefIdsAndScopeId(fieldIds, projectId)
                .stream()
                .collect(Collectors.toMap(CustomField::getRefId, CustomField::getId));
        // 根据组织字段ID，替换为项目字段ID
        return customFields.stream()
                .map(item -> {
                    TemplateCustomFieldRequest request = new TemplateCustomFieldRequest();
                    BeanUtils.copyProperties(item, request);
                    request.setFieldId(refFieldMap.get(item.getFieldId()));
                    return request;
                })
                .filter(item -> StringUtils.isNotBlank(item.getFieldId()))
                .toList();
    }

    @Override
    public Template getWithCheck(String id) {
        return checkResourceExist(id);
    }

    @Override
    public List<Template> list(String scopeId, String scene) {
        checkScene(scene);
        List<Template> templates = getTemplates(scopeId, scene);
        translateInternalTemplate(templates);
        return templates;
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new MSException(TEMPLATE_SCENE_ILLEGAL));
    }

    @Override
    public List<Template> getTemplates(String scopeId, String scene) {
        QueryChain<Template> queryChain = queryChain().where(TEMPLATE.SCOPE_ID.eq(scopeId)
                .and(TEMPLATE.SCENE.eq(scene)));
        return mapper.selectListByQuery(queryChain);
    }

    @Override
    public TemplateDTO getTemplateDTO(Template template) {
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        // 查找字段名称
        List<String> fieldIds = templateCustomFields.stream().map(TemplateCustomField::getFieldId).toList();
        List<CustomField> customFields = baseCustomFieldService.getByIds(fieldIds);
        Map<String, CustomField> fieldMap = customFields
                .stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));
        List<TemplateCustomFieldDTO> customFieldDTOList = templateCustomFields.stream()
                .filter(item -> !BooleanUtils.isTrue(item.getSystemField()) && fieldMap.containsKey(item.getFieldId()))
                .sorted(Comparator.comparingInt(TemplateCustomField::getPos))
                .map(item -> {
                    CustomField customField = fieldMap.get(item.getFieldId());
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    BeanUtils.copyProperties(item, templateCustomFieldDTO);
                    templateCustomFieldDTO.setFieldName(customField.getName());
                    if (BooleanUtils.isTrue(customField.getInternal())) {
                        templateCustomFieldDTO.setInternalFieldKey(customField.getName());
                        templateCustomFieldDTO.setFieldName(baseCustomFieldService.translateInternalField(customField.getName()));
                    }
                    templateCustomFieldDTO.setType(customField.getType());
                    templateCustomFieldDTO.setInternal(customField.getInternal());
                    AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                    Object defaultValue = null;
                    try {
                        defaultValue = customFieldResolver.parse2Value(item.getDefaultValue());
                    } catch (Exception e) {
                        LogUtils.error(e);
                    }
                    templateCustomFieldDTO.setDefaultValue(defaultValue);
                    return templateCustomFieldDTO;
                }).toList();
        List<String> ids = customFieldDTOList.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> fieldOptions = baseCustomFieldOptionService.getByFieldIds(ids);
        Map<String, List<CustomFieldOption>> collect = fieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        customFieldDTOList.forEach(item -> {
            item.setOptions(collect.get(item.getFieldId()));
        });
        // 封装系统字段信息
        List<TemplateCustomFieldDTO> systemFieldDTOS = templateCustomFields.stream()
                .filter(i -> BooleanUtils.isTrue(i.getSystemField()))
                .map(i -> {
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    templateCustomFieldDTO.setFieldId(i.getFieldId());
                    templateCustomFieldDTO.setDefaultValue(i.getDefaultValue());
                    return templateCustomFieldDTO;
                }).toList();

        List<String> sysIds = systemFieldDTOS.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> sysFieldOptions = baseCustomFieldOptionService.getByFieldIds(sysIds);
        Map<String, List<CustomFieldOption>> sysCollect = sysFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        systemFieldDTOS.forEach(item -> {
            item.setOptions(sysCollect.get(item.getFieldId()));
        });
        TemplateDTO result = new TemplateDTO();
        BeanUtils.copyProperties(template, result);

        result.setCustomFields(customFieldDTOList);
        result.setSystemFields(systemFieldDTOS);
        return result;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id) {
        Template template = checkResourceExist(id);
        checkInternal(template);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
        baseTemplateCustomFieldService.deleteByTemplateId(id);
    }

    protected void checkInternal(Template template) {
        if (template.getInternal()) {
            throw new MSException(INTERNAL_TEMPLATE_PERMISSION);
        }
    }

    public List<Template> translateInternalTemplate(List<Template> templates) {
        templates.forEach(item -> {
            if (item.getInternal()) {
                item.setName(translateInternalTemplate());
            }
        });
        return templates;
    }

    @Override
    public String translateInternalTemplate() {
        return Translator.get("template.default");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void initFunctionalDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        // 初始化字段
        List<CustomField> customFields = baseCustomFieldService.initFunctionalDefaultCustomField(scopeType, scopeId);
        // 初始化模板
        Template template = initDefaultTemplate(scopeId, "functional_default", scopeType, TemplateScene.FUNCTIONAL);
        // 初始化模板和字段的关联关系
        List<TemplateCustomFieldRequest> templateCustomFieldRequests = customFields.stream().map(customField -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            templateCustomFieldRequest.setRequired(true);
            templateCustomFieldRequest.setFieldId(customField.getId());
            return templateCustomFieldRequest;
        }).toList();
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), templateCustomFieldRequests);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void initUiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "ui_default", scopeType, TemplateScene.UI);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void initTestPlanDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "test_plan_default", scopeType, TemplateScene.TEST_PLAN);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByScopeId(String scopeId) {
        QueryChain<Template> queryChain = queryChain().where(TEMPLATE.SCOPE_ID.eq(scopeId));
        List<String> ids = queryChain.list().stream().map(Template::getId).toList();
        // 删除模板
        LogicDeleteManager.execWithoutLogicDelete(() ->
                mapper.deleteByQuery(queryChain));
        // 删除模板和字段的关联关系
        baseTemplateCustomFieldService.deleteByTemplateIds(ids);
    }

    @Override
    public void initApiDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        initDefaultTemplate(scopeId, "api_default", scopeType, TemplateScene.API);
    }

    @Override
    public void initBugDefaultTemplate(String scopeId, TemplateScopeType scopeType) {
        // 初始化字段
        List<CustomField> customFields = baseCustomFieldService.initBugDefaultCustomField(scopeType, scopeId);
        // 初始化模板
        Template template = this.initDefaultTemplate(scopeId, "bug_default", scopeType, TemplateScene.BUG);
        // 初始化模板和字段的关联关系
        List<TemplateCustomFieldRequest> templateCustomFieldRequests = customFields.stream().map(customField -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            templateCustomFieldRequest.setRequired(true);
            templateCustomFieldRequest.setFieldId(customField.getId());
            return templateCustomFieldRequest;
        }).toList();
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), templateCustomFieldRequests);
    }

    private Template initDefaultTemplate(String scopeId, String name, TemplateScopeType scopeType, TemplateScene scene) {
        Template template = new Template();
        template.setName(name);
        template.setInternal(true);
        template.setCreateUser("admin");
        template.setScopeType(scopeType.name());
        template.setScopeId(scopeId);
        template.setEnableThirdPart(false);
        template.setScene(scene.name());
        mapper.insert(template);
        return template;
    }

    private Template checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_template.name");
    }

    private void checkAddExist(Template template) {
        boolean exists = queryChain().where(TEMPLATE.SCOPE_ID.eq(template.getScopeId())
                        .and(TEMPLATE.NAME.eq(template.getName()))
                        .and(TEMPLATE.SCENE.eq(template.getScene())))
                .exists();
        if (exists) {
            throw new MSException(TEMPLATE_EXIST);
        }
    }

    private List<TemplateCustomFieldRequest> parse2TemplateCustomFieldRequests(List<TemplateSystemCustomFieldRequest> systemFields) {
        if (CollectionUtils.isEmpty(systemFields)) {
            return List.of();
        }
        return systemFields.stream().map(f -> {
            TemplateCustomFieldRequest request = new TemplateCustomFieldRequest();
            BeanUtils.copyProperties(f, request);
            request.setRequired(false);
            return request;
        }).toList();
    }
}
