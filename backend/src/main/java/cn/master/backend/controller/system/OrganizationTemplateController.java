package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.Template;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.OrganizationTemplateService;
import cn.master.backend.service.log.OrganizationTemplateLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
@RestController
@RequestMapping("/organization/template")
@Tag(name = "系统设置-组织-模版")
@RequiredArgsConstructor
public class OrganizationTemplateController {
    private final OrganizationTemplateService organizationTemplateService;

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.addLog(#request)", logClass = OrganizationTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.add(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#logClass.updateLog(#request)", logClass = OrganizationTemplateLogService.class)
    //@CheckOrgOwner(resourceId = "#request.getId()", resourceType = "template", resourceCol = "scope_id")
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.update(request);
    }

    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取模版列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String organizationId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return organizationTemplateService.list(organizationId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    //@CheckOrgOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public TemplateDTO get(@PathVariable String id) {
        return organizationTemplateService.getTemplateWithCheck(id);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = OrganizationTemplateLogService.class)
    //@CheckOrgOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        organizationTemplateService.delete(id);
    }

    @GetMapping("/disable/{organizationId}/{scene}")
    @Operation(summary = "关闭组织模板，开启项目模板")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ENABLE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.disableOrganizationTemplateLog(#organizationId,#scene)", logClass = OrganizationTemplateLogService.class)
    public void disableOrganizationTemplate(@PathVariable String organizationId, @PathVariable String scene) {
        organizationTemplateService.disableOrganizationTemplate(organizationId, scene);
    }

    @GetMapping("/enable/config/{organizationId}")
    @Operation(summary = "是否启用组织模版")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(@PathVariable String organizationId) {
        return organizationTemplateService.getOrganizationTemplateEnableConfig(organizationId);
    }
}
