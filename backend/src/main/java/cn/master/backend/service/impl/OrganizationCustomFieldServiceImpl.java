package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.entity.Project;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TemplateCustomFieldMapper;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import cn.master.backend.service.BaseCustomFieldOptionService;
import cn.master.backend.service.BaseOrganizationParameterService;
import cn.master.backend.service.OrganizationCustomFieldService;
import cn.master.backend.service.OrganizationService;
import cn.master.backend.util.SubListUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.backend.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.handler.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@Service
public class OrganizationCustomFieldServiceImpl extends BaseCustomFieldServiceImpl implements OrganizationCustomFieldService {
    private final OrganizationService organizationService;

    public OrganizationCustomFieldServiceImpl(BaseOrganizationParameterService baseOrganizationParameterService,
                                              OrganizationService organizationService,
                                              BaseCustomFieldOptionService baseCustomFieldOptionService,
                                              TemplateCustomFieldMapper templateCustomFieldMapper) {
        super(baseOrganizationParameterService, baseCustomFieldOptionService, templateCustomFieldMapper);
        this.organizationService = organizationService;
    }

    @Override
    public List<CustomFieldDTO> list(String scopeId, String scene) {
        organizationService.checkResourceExist(scopeId);
        return super.list(scopeId, scene);
    }

    @Override
    public CustomFieldDTO getCustomFieldWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldWithCheck(id);
        organizationService.checkResourceExist(customField.getScopeId());
        return customField;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        organizationService.checkResourceExist(customField.getScopeId());
        customField.setScopeType(TemplateScopeType.ORGANIZATION.name());
        customField = super.add(customField, options);
        // 同步创建项目级别字段
        addRefProjectCustomField(customField, options);
        return customField;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        checkOrganizationTemplateEnable(customField.getScopeId(), originCustomField.getScene());
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        organizationService.checkResourceExist(originCustomField.getScopeId());
        // 同步创建项目级别字段
        updateRefProjectCustomField(customField, options);
        return super.update(customField, options);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        checkInternal(customField);
        organizationService.checkResourceExist(customField.getScopeId());
        // 同步删除项目级别字段
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    private void deleteRefProjectTemplate(String orgCustomFieldId) {
        LogicDeleteManager.execWithoutLogicDelete(() ->
                mapper.deleteByQuery(queryChain().where(CUSTOM_FIELD.REF_ID.eq(orgCustomFieldId))));
        // 删除字段选项
        List<String> projectCustomFieldIds = queryChain().select(CUSTOM_FIELD.ID).from(CUSTOM_FIELD)
                .where(CUSTOM_FIELD.REF_ID.eq(orgCustomFieldId)).listAs(String.class);
        // 分批删除
        SubListUtils.dealForSubList(projectCustomFieldIds, 100, baseCustomFieldOptionService::deleteByFieldIds);
    }


    private void updateRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        List<CustomField> projectFields = getByRefId(orgCustomField.getId());
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        projectFields.forEach(projectField -> {
            customField.setId(projectField.getId());
            customField.setScopeId(projectField.getScopeId());
            customField.setRefId(orgCustomField.getId());
            customField.setScene(orgCustomField.getScene());
            super.update(customField, options);
        });
    }

    private List<CustomField> getByRefId(String refId) {
        return queryChain().where(CUSTOM_FIELD.REF_ID.eq(refId)).list();
    }

    private void addRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        String orgId = orgCustomField.getScopeId();
        List<String> projectIds = QueryChain.of(Project.class).select(PROJECT.ID).from(PROJECT)
                .where(PROJECT.ORGANIZATION_ID.eq(orgId)).listAs(String.class);
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        projectIds.forEach(projectId -> {
            customField.setScopeType(TemplateScopeType.PROJECT.name());
            customField.setScopeId(projectId);
            customField.setRefId(orgCustomField.getId());
            super.baseAdd(customField, customFieldOptions);
        });
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }
}
