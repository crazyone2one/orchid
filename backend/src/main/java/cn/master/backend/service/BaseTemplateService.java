package cn.master.backend.service;

import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.Template;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import cn.master.backend.payload.request.system.template.TemplateSystemCustomFieldRequest;
import com.mybatisflex.core.service.IService;
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

    /**
     * * 初始化功能用例模板
     * * 创建组织的时候调用初始化组织模板
     * * 创建项目的时候调用初始化项目模板
     *
     * @param scopeId
     * @param scopeType
     */
    void initFunctionalDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    /**
     * * 初始化缺陷模板
     * * 创建组织的时候调用初始化组织模板
     * * 创建项目的时候调用初始化项目模板
     *
     * @param scopeId
     * @param scopeType
     */
    void initBugDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initApiDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initUiDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void initTestPlanDefaultTemplate(String scopeId, TemplateScopeType scopeType);

    void deleteByScopeId(String projectId);
}
