package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import cn.master.backend.service.OrganizationProjectService;
import cn.master.backend.service.log.OrganizationProjectLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@RestController
@Tag(name = "系统设置-组织-项目")
@RequiredArgsConstructor
@RequestMapping("/organization/project")
public class OrganizationProjectController {
    private final OrganizationProjectService organizationProjectService;

    @PostMapping("/add")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = OrganizationProjectLogService.class)
    @Operation(summary = "系统设置-组织-项目-创建项目")
    public ProjectDTO addProject(@RequestBody @Validated({Created.class}) AddProjectRequest request) {
        return organizationProjectService.add(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "系统设置-组织-项目-根据ID获取项目信息")
    @Parameter(name = "id", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    //@CheckOwner(resourceId = "#id", resourceType = "project")
    public ProjectDTO getProject(@PathVariable @NotBlank String id) {
        return organizationProjectService.get(id);
    }

    @PostMapping("/page")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-获取项目列表")
    //@CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Page<ProjectDTO> getProjectList(@Validated @RequestBody OrganizationProjectRequest request) {
        return organizationProjectService.getProjectPage(request);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-项目-编辑")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    //@CheckOwner(resourceId = "#request.id", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = OrganizationProjectLogService.class)
    public ProjectDTO updateProject(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return organizationProjectService.update(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/delete/{id}")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_DELETE)
    @Operation(summary = "系统设置-组织-项目-删除")
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = OrganizationProjectLogService.class)
    //@CheckOwner(resourceId = "#id", resourceType = "project")
    public int deleteProject(@PathVariable String id) {
        return organizationProjectService.delete(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/revoke/{id}")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_RECOVER)
    @Operation(summary = "系统设置-组织-项目-撤销删除")
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.recoverLog(#id)", logClass = OrganizationProjectLogService.class)
    @Parameter(name = "id", description = "项目", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    //@CheckOwner(resourceId = "#id", resourceType = "project")
    public int revokeProject(@PathVariable String id) {
        return organizationProjectService.revoke(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-组织-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#id)", logClass = OrganizationProjectLogService.class)
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    //@CheckOwner(resourceId = "#id", resourceType = "project")
    public void enable(@PathVariable String id) {
        organizationProjectService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-组织-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#id)", logClass = OrganizationProjectLogService.class)
    //@CheckOwner(resourceId = "#id", resourceType = "project")
    public void disable(@PathVariable String id) {
        organizationProjectService.disable(id);
    }

    @PostMapping("/member-list")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-组织-项目-成员列表")
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Page<UserExtendDTO> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        return organizationProjectService.getProjectMember(request);
    }

    @PostMapping("/add-members")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-组织-项目-添加成员")
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        ProjectAddMemberBatchRequest batchRequest = new ProjectAddMemberBatchRequest();
        batchRequest.setProjectIds(List.of(request.getProjectId()));
        batchRequest.setUserIds(request.getUserIds());
        organizationProjectService.addProjectMember(batchRequest);
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-组织-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#projectId)", logClass = OrganizationProjectLogService.class)
    //@CheckOwner(resourceId = "#projectId", resourceType = "project")
    public int removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        return organizationProjectService.removeProjectMember(projectId, userId, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/user-admin-list/{organizationId}")
    @Operation(summary = "系统设置-组织-项目-获取项目管理员下拉选项")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    //@CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<UserExtendDTO> getUserAdminList(@PathVariable String organizationId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserAdminList(organizationId, keyword);
    }

    @GetMapping("/user-member-list/{organizationId}/{projectId}")
    @Operation(summary = "系统设置-组织-项目-获取成员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    //@CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<UserExtendDTO> getUserMemberList(@PathVariable String organizationId, @PathVariable String projectId,
                                                 @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationProjectService.getUserMemberList(organizationId, projectId, keyword);
    }

    @PostMapping("/pool-options")
    @Operation(summary = "系统设置-组织-项目-获取资源池下拉选项")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    //@CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public List<OptionDTO> getProjectOptions(@Validated @RequestBody ProjectPoolRequest request) {
        return organizationProjectService.getTestResourcePoolOptions(request);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-组织-项目-修改项目名称")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.renameLog(#request)", logClass = OrganizationProjectLogService.class)
    //@CheckOwner(resourceId = "#request.getId()", resourceType = "project")
    public void rename(@RequestBody @Validated({Updated.class}) UpdateProjectNameRequest request) {
        organizationProjectService.rename(request, SessionUtils.getCurrentUserId());
    }
}
