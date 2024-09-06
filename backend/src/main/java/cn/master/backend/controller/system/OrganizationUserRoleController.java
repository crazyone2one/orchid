package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationUserRoleEditRequest;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberEditRequest;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.OrganizationUserRoleService;
import cn.master.backend.service.log.OrganizationUserRoleLogService;
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
 * @author Created by 11's papa on 08/08/2024
 **/
@Tag(name = "系统设置-组织-用户组")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/role/organization")
public class OrganizationUserRoleController {
    private final OrganizationUserRoleService organizationUserRoleService;

    @GetMapping("/list/{organizationId}")
    @Operation(summary = "系统设置-组织-用户组-获取用户组列表")
    @Parameter(name = "organizationId", description = "当前组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<UserRole> list(@PathVariable String organizationId) {
        return organizationUserRoleService.list(organizationId);
    }

    @PostMapping("/add")
    @Operation(summary = "系统设置-组织-用户组-添加用户组")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = OrganizationUserRoleLogService.class)
    public UserRole add(@Validated({Created.class}) @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getCurrentUserId());
        BeanUtils.copyProperties(request, userRole);
        return organizationUserRoleService.add(userRole);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-用户组-修改用户组")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = OrganizationUserRoleLogService.class)
    //@CheckOrgOwner(resourceId = "#request.getId()", resourceType = "user_role", resourceCol = "scope_id")
    public UserRole update(@Validated({Updated.class}) @RequestBody OrganizationUserRoleEditRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return organizationUserRoleService.update(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-组织-用户组-删除用户组")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_DELETE)
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = OrganizationUserRoleLogService.class)
    //@CheckOrgOwner(resourceId = "#id", resourceType = "user_role", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        organizationUserRoleService.delete(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "系统设置-组织-用户组-获取用户组对应的权限配置")
    @Parameter(name = "id", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return organizationUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "系统设置-组织-用户组-修改用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updatePermissionSettingLog(#request)", logClass = OrganizationUserRoleLogService.class)
    //@CheckOrgOwner(resourceId = "#request.getUserRoleId()", resourceType = "user_role", resourceCol = "scope_id")
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        organizationUserRoleService.updatePermissionSetting(request);
    }

    @GetMapping("/get-member/option/{organizationId}/{roleId}")
    @Operation(summary = "系统设置-组织-用户组-获取成员下拉选项")
    @HasAnyAuthorize(value = {PermissionConstants.ORGANIZATION_USER_ROLE_READ})
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "roleId", description = "用户组ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    public List<UserExtendDTO> getMember(@PathVariable String organizationId,
                                         @PathVariable String roleId,
                                         @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                         @RequestParam(required = false) String keyword) {
        return organizationUserRoleService.getMember(organizationId, roleId, keyword);
    }

    @PostMapping("/list-member")
    @Operation(summary = "系统设置-组织-用户组-获取成员列表")
    @HasAnyAuthorize(value = {PermissionConstants.ORGANIZATION_USER_ROLE_READ})
    public Page<User> listMember(@Validated @RequestBody OrganizationUserRoleMemberRequest request) {
        return organizationUserRoleService.listMember(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-组织-用户组-添加用户组成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.editMemberLog(#request)", logClass = OrganizationUserRoleLogService.class)
    public void addMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.addMember(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/remove-member")
    @Operation(summary = "系统设置-组织-用户组-删除用户组成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.editMemberLog(#request)", logClass = OrganizationUserRoleLogService.class)
    public void removeMember(@Validated @RequestBody OrganizationUserRoleMemberEditRequest request) {
        organizationUserRoleService.removeMember(request);
    }
}
