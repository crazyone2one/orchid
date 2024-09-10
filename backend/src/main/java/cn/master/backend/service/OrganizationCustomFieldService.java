package cn.master.backend.service;

import cn.master.backend.entity.CustomField;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import jakarta.validation.Valid;

import java.util.List;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
public interface OrganizationCustomFieldService extends BaseCustomFieldService {
    @Override
    List<CustomFieldDTO> list(String scopeId, String scene);

    @Override
    CustomFieldDTO getCustomFieldWithCheck(String id);

    @Override
    CustomField add(CustomField customField, @Valid List<CustomFieldOptionRequest> options);

    @Override
    CustomField update(CustomField customField, @Valid List<CustomFieldOptionRequest> options);

    @Override
    void delete(String id);
}
