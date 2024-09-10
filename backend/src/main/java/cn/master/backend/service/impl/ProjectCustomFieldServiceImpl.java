package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.Project;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.mapper.TemplateCustomFieldMapper;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import cn.master.backend.service.BaseCustomFieldOptionService;
import cn.master.backend.service.BaseOrganizationParameterService;
import cn.master.backend.service.ProjectCustomFieldService;
import cn.master.backend.util.CommonBeanFactory;
import cn.master.backend.util.ServiceUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.master.backend.handler.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Service
public class ProjectCustomFieldServiceImpl extends BaseCustomFieldServiceImpl implements ProjectCustomFieldService {

    public ProjectCustomFieldServiceImpl(BaseOrganizationParameterService baseOrganizationParameterService,
                                         BaseCustomFieldOptionService baseCustomFieldOptionService,
                                         TemplateCustomFieldMapper templateCustomFieldMapper) {
        super(baseOrganizationParameterService, baseCustomFieldOptionService, templateCustomFieldMapper);
    }

    @Override
    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkProjectResourceExist(scopeId);
        return super.list(scopeId, scene);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkInternal(customField);
        Project project = checkProjectResourceExist(customField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), customField.getScene());
        super.delete(id);
    }

    @Override
    public CustomFieldDTO getCustomFieldWithCheck(String id) {
        CustomFieldDTO withCheck = super.getCustomFieldWithCheck(id);
        checkProjectResourceExist(withCheck.getScopeId());
        return withCheck;
    }

    @Override
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        Project project = checkProjectResourceExist(customField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), customField.getScene());
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        return super.add(customField, options);
    }

    @Override
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        Project project = checkProjectResourceExist(originCustomField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), originCustomField.getScene());
        return super.update(customField, options);
    }

    private void checkProjectTemplateEnable(String orgId, String scene) {
        if (isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(PROJECT_TEMPLATE_PERMISSION);
        }
    }

    private Project checkProjectResourceExist(String scopeId) {
        return ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(ProjectMapper.class)).selectOneById(scopeId), "permission.project.name");
    }


}
