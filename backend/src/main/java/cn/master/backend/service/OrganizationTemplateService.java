package cn.master.backend.service;

import cn.master.backend.entity.Template;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
public interface OrganizationTemplateService extends BaseTemplateService {
    Template add(TemplateUpdateRequest request, String creator);

    Template update(TemplateUpdateRequest request);

    void checkOrganizationTemplateEnable(String orgId, String scene);

    void addRefProjectTemplate(Template orgTemplate, @Valid List<TemplateCustomFieldRequest> customFields, @Valid List<TemplateSystemCustomFieldRequest> systemFields);

    void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    @Override
    List<Template> list(String organizationId, String scene);

    TemplateDTO getTemplateWithCheck(String id);

    @Override
    void delete(String id);

    void disableOrganizationTemplate(String orgId, String scene);

    Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId);
}
