package cn.master.backend.controller.project;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.project.ProjectUserRoleDTO;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.project.ProjectUserRoleEditRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberEditRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.ProjectUserRoleService;
import cn.master.backend.service.log.ProjectUserRoleLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Tag(name = "项目管理-项目与权限-用户组")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/role/project")
public class ProjectUserRoleController {
    private final ProjectUserRoleService projectUserRoleService;

    @PostMapping("/add")
    @Operation(summary = "项目管理-项目与权限-用户组-添加用户组")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = ProjectUserRoleLogService.class)
    public UserRole add(@Validated({Created.class}) @RequestBody ProjectUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getCurrentUserId());
        BeanUtils.copyProperties(request, userRole);
        return projectUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-项目与权限-用户组-修改用户组")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = ProjectUserRoleLogService.class)
    //@CheckProjectOwner(resourceId = "#request.getId()", resourceType = "user_role", resourceCol = "scope_id")
    public UserRole update(@Validated({Updated.class}) @RequestBody ProjectUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return projectUserRoleService.update(userRole);
    }

    @PostMapping("/list")
    @Operation(summary = "项目管理-项目与权限-用户组-获取用户组列表")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_READ)
    public Page<ProjectUserRoleDTO> list(@Validated @RequestBody ProjectUserRoleRequest request) {
        return projectUserRoleService.listPage(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-项目与权限-用户组-删除用户组")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_DELETE)
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = ProjectUserRoleLogService.class)
    //@CheckProjectOwner(resourceId = "#id", resourceType = "user_role", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        projectUserRoleService.delete(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "项目管理-项目与权限-用户组-获取用户组对应的权限配置")
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return projectUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "项目管理-项目与权限-用户组-修改用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updatePermissionSettingLog(#request)", logClass = ProjectUserRoleLogService.class)
    //@CheckProjectOwner(resourceId = "#request.getUserRoleId()", resourceType = "user_role", resourceCol = "scope_id")
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        projectUserRoleService.updatePermissionSetting(request);
    }

    @GetMapping("/get-member/option/{projectId}/{roleId}")
    @Operation(summary = "项目管理-项目与权限-用户组-获取成员下拉选项")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_READ)
    @Parameters({
            @Parameter(name = "projectId", description = "当前项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "roleId", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    public List<UserExtendDTO> getMember(@PathVariable String projectId,
                                         @PathVariable String roleId,
                                         @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                         @RequestParam(required = false) String keyword) {
        return projectUserRoleService.getMember(projectId, roleId, keyword);
    }

    @PostMapping("/list-member")
    @Operation(summary = "项目管理-项目与权限-用户组-获取成员列表")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_READ)
    public Page<User> listMember(@Validated @RequestBody ProjectUserRoleMemberRequest request) {
        return projectUserRoleService.listMember(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "项目管理-项目与权限-用户组-添加用户组成员")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.editMemberLog(#request)", logClass = ProjectUserRoleLogService.class)
    public void addMember(@Validated @RequestBody ProjectUserRoleMemberEditRequest request) {
        projectUserRoleService.addMember(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "项目管理-项目与权限-用户组-删除用户组成员")
    @HasAuthorize(PermissionConstants.PROJECT_GROUP_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.editMemberLog(#request)", logClass = ProjectUserRoleLogService.class)
    public void removeMember(@Validated @RequestBody ProjectUserRoleMemberEditRequest request) {
        projectUserRoleService.removeMember(request);
    }
}
