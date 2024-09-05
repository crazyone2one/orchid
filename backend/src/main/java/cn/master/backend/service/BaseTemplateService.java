package cn.master.backend.service;

import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.Template;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 模版 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
public interface BaseTemplateService extends IService<Template> {
    Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    Template update(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    Template add(Template template, @Valid List<TemplateCustomFieldRequest> customFields, @Valid List<TemplateSystemCustomFieldRequest> systemFields);

    List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields);

    Template getWithCheck(String id);

    List<Template> list(String organizationId, String scene);

    List<Template> getTemplates(String scopeId, String scene);

    TemplateDTO getTemplateDTO(Template template);

    void delete(String id);

    String translateInternalTemplate();
}
