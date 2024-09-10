package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.CustomField;
import cn.master.backend.mapper.CustomFieldMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.CustomFieldUpdateRequest;
import cn.master.backend.util.EnumValidator;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationCustomFieldLogService {
    @Resource
    CustomFieldMapper customFieldMapper;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_FIELD;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_FIELD;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_FIELD;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_FIELD;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_FIELD;
            default -> null;
        };
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = customFieldMapper.selectOneById(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        CustomField customField = customFieldMapper.selectOneById(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }
}
