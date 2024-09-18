package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScene;
import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.*;
import cn.master.backend.handler.resolver.field.AbstractCustomFieldResolver;
import cn.master.backend.handler.resolver.field.CustomFieldResolverFactory;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseTemplateCustomFieldService;
import cn.master.backend.service.BaseTemplateService;
import cn.master.backend.service.CreateProjectResourceService;
import cn.master.backend.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
public class CreateTemplateResourceService implements CreateProjectResourceService {
    @Resource
    ProjectMapper projectMapper;
    @Resource
    BaseTemplateService baseTemplateService;
    @Resource
    BaseCustomFieldService baseCustomFieldService;
    @Resource
    BaseCustomFieldOptionServiceImpl baseCustomFieldOptionService;
    @Resource
    BaseTemplateCustomFieldService baseTemplateCustomFieldService;


    @Override
    public void createResources(String projectId) {
        Project project = projectMapper.selectOneById(projectId);
        if (project == null) {
            return;
        }
        String organizationId = project.getOrganizationId();
        for (TemplateScene scene : TemplateScene.values()) {
            if (baseTemplateService.isOrganizationTemplateEnable(organizationId, scene.name())) {
                // 如果没有开启项目模板，则根据组织模板创建项目模板
                // 先创建字段再创建模板
                createProjectCustomField(projectId, organizationId, scene);
                createProjectTemplate(projectId, organizationId, scene);
                createProjectStatusSetting(projectId, organizationId, scene);
            } else {
                // 开启了项目模板，则初始化项目模板和字段
                initProjectTemplate(projectId, scene);
            }
        }
    }

    private void initProjectTemplate(String projectId, TemplateScene scene) {
        switch (scene) {
            case FUNCTIONAL:
                baseTemplateService.initFunctionalDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case BUG:
                baseTemplateService.initBugDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                // todo baseStatusFlowSettingService.initBugDefaultStatusFlowSetting(projectId, TemplateScopeType.PROJECT);
                break;
            case API:
                baseTemplateService.initApiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case UI:
                baseTemplateService.initUiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case TEST_PLAN:
                baseTemplateService.initTestPlanDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            default:
                break;
        }
    }

    private void createProjectStatusSetting(String projectId, String organizationId, TemplateScene scene) {
        // todo
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织模板创建项目模板
     *
     * @param projectId
     * @param organizationId
     * @param scene
     */
    private void createProjectTemplate(String projectId, String organizationId, TemplateScene scene) {
// 同步创建项目级别模板
        List<Template> orgTemplates = baseTemplateService.getTemplates(organizationId, scene.name());
        List<String> orgTemplateIds = orgTemplates.stream().map(Template::getId).toList();
        Map<String, List<TemplateCustomField>> templateCustomFieldMap = baseTemplateCustomFieldService.getByTemplateIds(orgTemplateIds)
                .stream()
                .collect(Collectors.groupingBy(TemplateCustomField::getTemplateId));

        Map<String, CustomField> customFieldMap = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name()).stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));

        // 忽略默认值校验，可能有多选框的选项被删除，造成不合法数据
        BaseTemplateCustomFieldServiceImpl.VALIDATE_DEFAULT_VALUE.set(false);
        orgTemplates.forEach((template) -> {
            List<TemplateCustomField> templateCustomFields = templateCustomFieldMap.get(template.getId());
            templateCustomFields = templateCustomFields == null ? List.of() : templateCustomFields;
            List<TemplateCustomFieldRequest> templateCustomFieldRequests = templateCustomFields.stream()
                    .map(templateCustomField -> {
                        TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
                        BeanUtils.copyProperties(templateCustomField, templateCustomFieldRequest);
                        CustomField customField = customFieldMap.get(templateCustomField.getFieldId());
                        try {
                            if (StringUtils.isNotBlank(templateCustomField.getDefaultValue())) {
                                // 将字符串转成对应的对象，方便调用统一的创建方法
                                AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                                templateCustomFieldRequest.setDefaultValue(customFieldResolver.parse2Value(templateCustomField.getDefaultValue()));
                            }
                        } catch (Exception e) {
                            BaseTemplateCustomFieldServiceImpl.VALIDATE_DEFAULT_VALUE.remove();
                            LogUtils.error(e);
                            templateCustomFieldRequest.setDefaultValue(null);
                        }
                        return templateCustomFieldRequest;
                    })
                    .toList();
            addRefProjectTemplate(projectId, template, templateCustomFieldRequests, null);
        });
        BaseTemplateCustomFieldServiceImpl.VALIDATE_DEFAULT_VALUE.remove();
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织模板创建项目模板
     *
     * @param projectId
     * @param orgTemplate
     * @param customFields
     * @param systemCustomFields
     */
    private void addRefProjectTemplate(String projectId, Template orgTemplate, List<TemplateCustomFieldRequest> customFields,
                                       List<TemplateSystemCustomFieldRequest> systemCustomFields) {
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        template.setScopeId(projectId);
        template.setRefId(orgTemplate.getId());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        List<TemplateCustomFieldRequest> refCustomFields = baseTemplateService.getRefTemplateCustomFieldRequest(projectId, customFields);
        baseTemplateService.baseAdd(template, refCustomFields, systemCustomFields);
    }

    private void createProjectCustomField(String projectId, String organizationId, TemplateScene scene) {
        // 查询组织字段和选项
        List<CustomField> orgFields = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name());
        List<String> orgFieldIds = orgFields.stream().map(CustomField::getId).toList();
        Map<String, List<CustomFieldOption>> customFieldOptionMap = baseCustomFieldOptionService.getByFieldIds(orgFieldIds)
                .stream()
                .collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        orgFields.forEach((field) -> {
            List<CustomFieldOption> options = customFieldOptionMap.get(field.getId());
            addRefProjectCustomField(projectId, field, options);
        });
    }

    private void addRefProjectCustomField(String projectId, CustomField orgCustomField, List<CustomFieldOption> options) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        customField.setScopeId(projectId);
        customField.setRefId(orgCustomField.getId());
        baseCustomFieldService.baseAdd(customField, options);
    }
}
