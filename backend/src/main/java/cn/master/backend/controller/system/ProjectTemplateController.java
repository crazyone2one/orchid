package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.Template;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.template.ProjectTemplateDTO;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.system.template.TemplateUpdateRequest;
import cn.master.backend.service.ProjectTemplateService;
import cn.master.backend.service.log.ProjectTemplateLogService;
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
 * @author Created by 11's papa on 09/05/2024
 **/
@RestController
@RequestMapping("/project/template")
@Tag(name = "项目管理-模版")
@RequiredArgsConstructor
public class ProjectTemplateController {
    private final ProjectTemplateService projectTemplateService;
    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取模版列表")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<ProjectTemplateDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable String projectId,
                                         @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable String scene) {
        return projectTemplateService.list(projectId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    //@CheckProjectOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public TemplateDTO get(@PathVariable String id) {
        return projectTemplateService.getTemplateWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = ProjectTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateService.add(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = ProjectTemplateLogService.class)
    //@CheckProjectOwner(resourceId = "#request.getId()", resourceType = "template", resourceCol = "scope_id")
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateService.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = ProjectTemplateLogService.class)
    //@CheckProjectOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        projectTemplateService.delete(id);
    }

    @GetMapping("/set-default/{projectId}/{id}")
    @Operation(summary = "设置默认模板")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.setDefaultTemplateLog(#id)", logClass = ProjectTemplateLogService.class)
    public void setDefaultTemplate(@PathVariable String projectId, @PathVariable String id) {
        projectTemplateService.setDefaultTemplate(projectId, id);
    }

    @GetMapping("/enable/config/{projectId}")
    @Operation(summary = "是否启用组织模版")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public Map<String, Boolean> getProjectTemplateEnableConfig(@PathVariable String projectId) {
        return projectTemplateService.getProjectTemplateEnableConfig(projectId);
    }
}
