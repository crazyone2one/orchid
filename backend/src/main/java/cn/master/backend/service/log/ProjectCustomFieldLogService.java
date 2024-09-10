package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.CustomField;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.CustomFieldUpdateRequest;
import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.util.EnumValidator;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectCustomFieldLogService {
    @Resource
    BaseCustomFieldService baseCustomFieldService;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                null,
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
            case API -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_FIELD;
            case FUNCTIONAL -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_FIELD;
            case UI -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_FIELD;
            case BUG -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_FIELD;
            case TEST_PLAN -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_FIELD;
        };
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = baseCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                null,
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
        CustomField customField = baseCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
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
