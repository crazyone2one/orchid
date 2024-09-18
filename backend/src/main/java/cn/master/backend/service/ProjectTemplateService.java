package cn.master.backend.service;

import cn.master.backend.entity.Template;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
public interface ProjectTemplateService extends BaseTemplateService {
    @Override
    List list(String projectId, String scene);

    TemplateDTO getTemplateWithCheck(String id);

    Template add(TemplateUpdateRequest request, String userId);

    Template update(TemplateUpdateRequest request);

    @Override
    void delete(String id);

    void setDefaultTemplate(String projectId, String id);

    Map<String, Boolean> getProjectTemplateEnableConfig(String projectId);

    TemplateDTO getTemplateDTOById(String templateId, String projectId, String scene);

    TemplateDTO getDefaultTemplateDTO(String projectId, String scene);
}
