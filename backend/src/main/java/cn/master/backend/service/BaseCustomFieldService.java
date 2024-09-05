package cn.master.backend.service;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.CustomField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Collection;
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
}
