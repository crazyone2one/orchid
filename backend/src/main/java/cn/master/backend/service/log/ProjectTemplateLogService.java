package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.Template;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.ProjectTemplateService;
import cn.master.backend.util.EnumValidator;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateLogService {
    @Resource
    ProjectTemplateService projectTemplateService;

    public LogDTO addLog(TemplateUpdateRequest request) {
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
            case API -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_TEMPLATE;
            case BUG -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_TEMPLATE;
            default -> null;
        };
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = projectTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    BooleanUtils.isTrue(template.getInternal()) ? Translator.get("template.default") : template.getName());
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO setDefaultTemplateLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    StringUtils.join(Translator.get("set_default_template"), ":",
                            template.getInternal() ? projectTemplateService.translateInternalTemplate() : template.getName()));
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                template.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(template.getScene()),
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}
