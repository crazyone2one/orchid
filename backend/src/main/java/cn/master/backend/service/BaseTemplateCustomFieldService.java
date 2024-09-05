package cn.master.backend.service;

import cn.master.backend.payload.request.system.template.TemplateCustomFieldRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.TemplateCustomField;

import java.util.List;

/**
 * 模板和字段的关联关系 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
public interface BaseTemplateCustomFieldService extends IService<TemplateCustomField> {
    void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests);

    void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests);

    void deleteByTemplateIdAndSystem(String templateId, boolean isSystem);

    List<TemplateCustomField> getByTemplateId(String id);

    void deleteByTemplateIds(List<String> projectTemplateIds);

    void deleteByTemplateId(String templateId);
}
