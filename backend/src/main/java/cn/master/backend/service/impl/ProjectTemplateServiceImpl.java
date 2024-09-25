package cn.master.backend.service.impl;

import cn.master.backend.constants.CustomFieldType;
import cn.master.backend.constants.ProjectApplicationType;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TemplateMapper;
import cn.master.backend.payload.dto.project.CustomFieldOptions;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.system.template.ProjectTemplateDTO;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.*;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.TemplateTableDef.TEMPLATE;
import static cn.master.backend.handler.result.CommonResultCode.DEFAULT_TEMPLATE_PERMISSION;
import static cn.master.backend.handler.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Service
public class ProjectTemplateServiceImpl extends BaseTemplateServiceImpl implements ProjectTemplateService {
    private final ProjectService projectService;
    private final ProjectApplicationService projectApplicationService;
    private final TemplateMapper templateMapper;

    public ProjectTemplateServiceImpl(BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                      BaseCustomFieldService baseCustomFieldService,
                                      BaseCustomFieldOptionService baseCustomFieldOptionService,
                                      ProjectService projectService,
                                      ProjectApplicationService projectApplicationService, TemplateMapper templateMapper) {
        super(baseTemplateCustomFieldService, baseCustomFieldService, baseCustomFieldOptionService);
        this.projectService = projectService;
        this.projectApplicationService = projectApplicationService;
        this.templateMapper = templateMapper;
    }

    @Override
    public List list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = super.list(projectId, scene);
        List<ProjectTemplateDTO> templateList = templates.stream().map(item -> {
            ProjectTemplateDTO dto = new ProjectTemplateDTO();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        // 缺陷模板需要获取第三方平台模板
        //if (StringUtils.equals(scene, TemplateScene.BUG.name())) {
        //    Template pluginBugTemplate = getPluginBugTemplate(projectId);
        //    if (pluginBugTemplate != null) {
        //        ProjectTemplateDTO pluginTemplate = BeanUtils.copyBean(new ProjectTemplateDTO(), pluginBugTemplate);
        //        pluginTemplate.setEnablePlatformDefault(true);
        //        templateDTOS.add(pluginTemplate);
        //    }
        //}
        // 标记默认模板
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(projectId, scene);
        ProjectTemplateDTO defaultTemplate = templateList.stream()
                .filter(t -> StringUtils.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            Optional<ProjectTemplateDTO> internalTemplate = templateList.stream()
                    .filter(ProjectTemplateDTO::getInternal).findFirst();
            if (internalTemplate.isPresent()) {
                defaultTemplate = internalTemplate.get();
            }
        }
        if (defaultTemplate != null) {
            defaultTemplate.setEnableDefault(true);
        }
        return templateList;
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        checkDefault(template);
        super.delete(id);
    }

    @Override
    public void setDefaultTemplate(String projectId, String id) {
        Template template = mapper.selectOneById(id);
        //if (template == null) {
        //    Template pluginBugTemplate = getPluginBugTemplate(projectId);
        //    if (pluginBugTemplate != null && StringUtils.equals(pluginBugTemplate.getId(), id)) {
        //        template = pluginBugTemplate;
        //    }
        //}
        if (template == null) {
            // 为空check抛出异常
            template = getWithCheck(id);
        }
        String paramType = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(template.getScene()).name();
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setTypeValue(id);
        projectApplication.setType(paramType);
        projectApplicationService.createOrUpdateConfig(projectApplication);
    }

    @Override
    public Map<String, Boolean> getProjectTemplateEnableConfig(String projectId) {
        projectService.checkResourceExist(projectId);
        ProjectDTO project = projectService.get(projectId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.FUNCTIONAL, TemplateScene.BUG)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), !isOrganizationTemplateEnable(project.getOrganizationId(), scene.name())));
        return templateEnableConfig;
    }

    @Override
    public TemplateDTO getTemplateDTOById(String templateId, String projectId, String scene) {
        AtomicReference<TemplateDTO> templateDTO = new AtomicReference<>(new TemplateDTO());
        Template template = templateMapper.selectOneById(templateId);
        Optional.ofNullable(template).ifPresentOrElse(item -> {
            templateDTO.set(super.getTemplateDTO(template));
        }, () -> {
            templateDTO.set(getDefaultTemplateDTO(projectId, scene));
        });
        return templateDTO.get();
    }

    @Override
    public TemplateDTO getDefaultTemplateDTO(String projectId, String scene) {
        String defaultTemplateId = getDefaultTemplateId(projectId, scene);
        Template template;
        if (StringUtils.isBlank(defaultTemplateId)) {
            // 如果没有默认模板，则获取内置模板
            template = getInternalTemplate(projectId, scene);
        } else {
            template = templateMapper.selectOneById(defaultTemplateId);
            if (template == null) {
                // 如果默认模板查不到，则获取内置模板
                template = getInternalTemplate(projectId, scene);
            }
        }
        TemplateDTO templateDTO = getTemplateDTO(template);
        List<UserExtendDTO> memberOption = projectService.getMemberOption(projectId, null);
        List<CustomFieldOption> memberCustomOption = memberOption.stream().map(option -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            customFieldOption.setFieldId(option.getId());
            customFieldOption.setValue(option.getId());
            customFieldOption.setInternal(false);
            customFieldOption.setText(option.getName());
            return customFieldOption;
        }).toList();
        templateDTO.getCustomFields().forEach(item -> {
            if (StringUtils.equalsAnyIgnoreCase(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                item.setOptions(memberCustomOption);
            }
        });
        return templateDTO;
    }

    @Override
    public List<CustomFieldOptions> getTableCustomField(String projectId, String scene) {
        List<Template> templates = QueryChain.of(templateMapper).where(TEMPLATE.SCOPE_ID.eq(projectId))
                .and(TEMPLATE.SCENE.eq(scene)).list();
        if (CollectionUtils.isNotEmpty(templates)) {
            List<String> templateIds = templates.stream().map(Template::getId).toList();
            List<TemplateCustomField> fieldList = baseTemplateCustomFieldService.getByTemplateIds(templateIds);
            List<String> fieldIds = fieldList.stream().map(TemplateCustomField::getFieldId).distinct().toList();
            List<CustomField> customFields = baseCustomFieldService.getByIds(fieldIds);
            List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(fieldIds);
            Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
            return customFields.stream().map(customField -> {
                CustomFieldOptions optionDTO = new CustomFieldOptions();
                optionDTO.setId(customField.getId());
                if (customField.getInternal()) {
                    optionDTO.setInternalFieldKey(customField.getName());
                    optionDTO.setName(baseCustomFieldService.translateInternalField(customField.getName()));
                } else {
                    optionDTO.setName(customField.getName());
                }
                optionDTO.setOptions(optionMap.get(customField.getId()));
                optionDTO.setInternal(customField.getInternal());
                optionDTO.setType(customField.getType());
                return optionDTO;
            }).collect(Collectors.toList());
        }
        return List.of();
    }

    private Template getInternalTemplate(String projectId, String scene) {
        return QueryChain.of(Template.class)
                .where(Template::getScopeId).eq(projectId)
                .and(Template::getScene).eq(scene)
                .and(Template::getInternal).eq(true)
                .one();
    }

    private void checkDefault(Template template) {
        String defaultTemplateId = getDefaultTemplateId(template.getScopeId(), template.getScene());
        if (StringUtils.equals(template.getId(), defaultTemplateId)) {
            throw new MSException(DEFAULT_TEMPLATE_PERMISSION);
        }
    }

    @Override
    public TemplateDTO getTemplateWithCheck(String id) {
        Template template = super.getWithCheck(id);
        projectService.checkResourceExist(template.getScopeId());
        TemplateDTO templateDTO = super.getTemplateDTO(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template add(TemplateUpdateRequest request, String userId) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(userId);
        projectService.checkResourceExist(template.getScopeId());
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        //saveUploadImages(request);
        return template;
    }

    @Override
    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkProjectTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        projectService.checkResourceExist(originTemplate.getScopeId());
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        //saveUploadImages(request);
        return template;
    }

    private void checkProjectTemplateEnable(String scopeId, String scene) {
        ProjectDTO project = projectService.get(scopeId);
        if (isOrganizationTemplateEnable(project.getOrganizationId(), scene)) {
            throw new MSException(PROJECT_TEMPLATE_PERMISSION);
        }
    }

    private String getDefaultTemplateId(String projectId, String scene) {
        ProjectApplicationType.DEFAULT_TEMPLATE defaultTemplateParam = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(scene);
        ProjectApplication projectApplication = projectApplicationService.getByType(projectId, defaultTemplateParam.name());
        return projectApplication == null ? null : projectApplication.getTypeValue();
    }
}
