package cn.master.backend.service;

import cn.master.backend.entity.CustomField;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;

import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
public interface ProjectCustomFieldService extends BaseCustomFieldService {
    @Override
    List<CustomFieldDTO> list(String projectId, String scene);

    @Override
    CustomFieldDTO getCustomFieldWithCheck(String id);

    @Override
    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    @Override
    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    @Override
    void delete(String id);
}
