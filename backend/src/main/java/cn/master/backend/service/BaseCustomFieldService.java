package cn.master.backend.service;

import cn.master.backend.constants.TemplateScopeType;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.CustomField;

import java.util.List;

/**
 * 自定义字段 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface BaseCustomFieldService extends IService<CustomField> {
    List<CustomField> getByIds(List<String> fieldIds);

    CustomField getWithCheck(String id);

    CustomField checkResourceExist(String id);

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    List<CustomField> getByRefIdsAndScopeId(List<String> fieldIds, String scopeId);

    String translateInternalField(String filedName);

    List<CustomFieldDTO> list(String scopeId, String scene);

    List<CustomField> getByScopeIdAndScene(String scopeId, String scene);

    CustomFieldDTO getCustomFieldWithCheck(String id);

    List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options);

    CustomField baseAdd(CustomField customField, List<CustomFieldOption> customFieldOptions);

    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    void delete(String id);

    List<CustomField> initFunctionalDefaultCustomField(TemplateScopeType scopeType, String scopeId);

    List<CustomField> initBugDefaultCustomField(TemplateScopeType scopeType, String scopeId);

    void deleteByScopeId(String scopeId);
}
