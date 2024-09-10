package cn.master.backend.service;

import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 自定义字段选项 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface BaseCustomFieldOptionService extends IService<CustomFieldOption> {

    List<CustomFieldOption> getByFieldId(String id);

    List<CustomFieldOption> getByFieldIds(List<String> fieldIds);

    void addByFieldId(String fieldId, List<CustomFieldOption> customFieldOptions);

    void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> customFieldOptionRequests);

    void deleteByFieldId(String fieldId);

    void deleteByFieldIds(List<String> fieldIds);
}
