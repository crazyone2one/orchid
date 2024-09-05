package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScene;
import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.OrganizationParameter;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.Template;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.*;
import cn.master.backend.util.SubListUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.TemplateTableDef.TEMPLATE;
import static cn.master.backend.handler.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
@Service
public class OrganizationTemplateServiceImpl extends BaseTemplateServiceImpl implements OrganizationTemplateService {
    private final OrganizationService organizationService;
    private final BaseOrganizationParameterService baseOrganizationParameterService;

    public OrganizationTemplateServiceImpl(BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                           OrganizationService organizationService,
                                           BaseCustomFieldService baseCustomFieldService,
                                           BaseCustomFieldOptionService baseCustomFieldOptionService,
                                           BaseOrganizationParameterService baseOrganizationParameterService) {
        super(baseTemplateCustomFieldService, baseCustomFieldService, baseCustomFieldOptionService);
        this.organizationService = organizationService;
        this.baseOrganizationParameterService = baseOrganizationParameterService;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template add(TemplateUpdateRequest request, String creator) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(creator);
        checkOrgResourceExist(template);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        // 同步创建项目级别模板
        addRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkOrganizationTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkOrgResourceExist(originTemplate);
        updateRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        template.setRefId(null);
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    private void saveUploadImages(TemplateUpdateRequest request) {
// todo 保存上传的文件
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addRefProjectTemplate(Template orgTemplate, @Valid List<TemplateCustomFieldRequest> customFields, @Valid List<TemplateSystemCustomFieldRequest> systemFields) {
        String orgId = orgTemplate.getScopeId();
        List<String> projectIds = QueryChain.of(Project.class).select(PROJECT.ID).from(PROJECT).where(PROJECT.ORGANIZATION_ID.eq(orgId)).listAs(String.class);
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectIds.forEach(projectId -> {
            template.setScopeId(projectId);
            template.setRefId(orgTemplate.getId());
            template.setScopeType(TemplateScopeType.PROJECT.name());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectId, customFields);
            baseAdd(template, refCustomFields, systemFields);
        });
    }

    @Override
    public void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        List<Template> projectTemplates = getByRefId(orgTemplate.getId());
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectTemplates.forEach(projectTemplate -> {
            template.setId(projectTemplate.getId());
            template.setScopeId(projectTemplate.getScopeId());
            template.setRefId(orgTemplate.getId());
            template.setScene(orgTemplate.getScene());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectTemplate.getScopeId(), customFields);
            super.update(template, refCustomFields, systemFields);
        });
    }

    @Override
    public List<Template> list(String organizationId, String scene) {
        organizationService.checkResourceExist(organizationId);
        return super.list(organizationId, scene);
    }

    @Override
    public TemplateDTO getTemplateWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkOrgResourceExist(template);
        TemplateDTO templateDTO = super.getTemplateDTO(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void disableOrganizationTemplate(String orgId, String scene) {
        if (StringUtils.isBlank(baseOrganizationParameterService.getValue(orgId, scene))) {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(orgId);
            organizationParameter.setParamKey(baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene));
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            baseOrganizationParameterService.save(organizationParameter);
        }
    }

    @Override
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId) {
        organizationService.checkResourceExist(organizationId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.FUNCTIONAL, TemplateScene.BUG)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), isOrganizationTemplateEnable(organizationId, scene.name())));
        return templateEnableConfig;
    }

    private void deleteRefProjectTemplate(String orgTemplateId) {
        QueryChain<Template> queryChain = queryChain().where(Template::getRefId).eq(orgTemplateId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
        // 删除项目模板和字段的关联关系

        List<String> projectTemplateIds = queryChain.select(TEMPLATE.ID).from(TEMPLATE)
                .where(TEMPLATE.REF_ID.eq(orgTemplateId)).listAs(String.class);
        // 分批删除
        SubListUtils.dealForSubList(projectTemplateIds, 100, baseTemplateCustomFieldService::deleteByTemplateIds);
    }

    private List<Template> getByRefId(String refId) {
        return queryChain().where(Template::getRefId).eq(refId).list();
    }

    @Override
    public void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }

    private void checkOrgResourceExist(Template template) {
        organizationService.checkResourceExist(template.getScopeId());
    }
}
