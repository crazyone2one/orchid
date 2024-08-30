package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.Project;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.project.ProjectSwitchRequest;
import cn.master.backend.payload.request.project.ProjectUpdateRequest;
import cn.master.backend.service.ProjectService;
import cn.master.backend.service.log.ProjectLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@RestController
@Tag(name = "项目管理")
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("update")
    @Operation(description = "项目管理-更新项目")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", logClass = ProjectLogService.class)
    public ProjectDTO update(@RequestBody @Validated({Updated.class}) ProjectUpdateRequest request) {

        return projectService.update(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/list/options/{organizationId}")
    @Operation(summary = "根据组织ID获取所有有权限的项目")
    public List<Project> getUserProject(@PathVariable String organizationId) {
        return projectService.getUserProject(organizationId, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/list/options/{organizationId}/{module}")
    @Operation(summary = "根据组织ID获取所有开启某个模块的所有有权限的项目")
    public List<Project> getUserProjectWidthModule(@PathVariable String organizationId, @PathVariable String module) {
        return projectService.getUserProjectWidthModule(organizationId, module, SessionUtils.getCurrentUserId());
    }

    @GetMapping("get/{id}")
    @Operation(summary = "项目管理-基本信息")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ)
    public ProjectDTO getInfo(@PathVariable String id) {
        return projectService.get(id);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ)
    public UserDTO switchProject(@RequestBody ProjectSwitchRequest request) {
        return projectService.switchProject(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/has-permission/{id}")
    @Operation(summary = "项目管理-获取当前用户是否有当前项目的权限")
    public boolean hasPermission(@PathVariable String id) {
        return projectService.hasPermission(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/get-member/option/{projectId}")
    @Operation(summary = "项目管理-获取成员下拉选项")
    @HasAuthorize(PermissionConstants.PROJECT_BASE_INFO_READ)
    public List<UserExtendDTO> getMemberOption(@PathVariable String projectId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return projectService.getMemberOption(projectId, keyword);
    }
}
