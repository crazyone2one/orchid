package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.Template;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.OrganizationTemplateService;
import cn.master.backend.util.EnumValidator;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateLogService {
    @Resource
    OrganizationTemplateService organizationTemplateService;

    public LogDTO addLog(TemplateUpdateRequest request) {
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
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_TEMPLATE;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_TEMPLATE;
        };
    }

    public LogDTO disableOrganizationTemplateLog(String organizationId, String scene) {
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                organizationId,
                scene,
                null,
                OperationLogType.UPDATE.name(),
                getDisableOrganizationTemplateModule(scene),
                Translator.get("project_template_enable"));
    }

    public String getDisableOrganizationTemplateModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN;
        };
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = organizationTemplateService.getTemplateWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
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
    public LogDTO deleteLog(String id) {
        Template template = organizationTemplateService.getTemplateWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
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
