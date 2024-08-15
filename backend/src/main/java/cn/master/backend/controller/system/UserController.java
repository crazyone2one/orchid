package cn.master.backend.controller.system;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.constants.UserSource;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.BasePageRequest;
import cn.master.backend.payload.dto.TableBatchProcessDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.user.UserBatchCreateResponse;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.system.OrganizationMemberBatchRequest;
import cn.master.backend.payload.request.system.user.UserBatchCreateRequest;
import cn.master.backend.payload.request.system.user.UserChangeEnableRequest;
import cn.master.backend.payload.request.system.user.UserEditRequest;
import cn.master.backend.payload.request.system.user.UserRoleBatchRelationRequest;
import cn.master.backend.payload.response.TableBatchProcessResponse;
import cn.master.backend.payload.response.user.UserSelectOption;
import cn.master.backend.payload.response.user.UserTableResponse;
import cn.master.backend.service.*;
import cn.master.backend.service.log.UserLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-用户")
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;
    private final GlobalUserRoleService globalUserRoleService;
    private final OrganizationService organizationService;
    private final GlobalUserRoleRelationService globalUserRoleRelationService;
    private final UserLogService userLogService;
    private final UserToolService userToolService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户-添加用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ADD)
    public UserBatchCreateResponse save(@Validated({Created.class}) @RequestBody UserBatchCreateRequest request) {
        return userService.addUser(request, UserSource.LOCAL.name(), SessionUtils.getCurrentUserId());
    }

    @PostMapping("/delete")
    @Operation(summary = "系统设置-系统-用户-删除用户")
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#request)", logClass = UserLogService.class)
    @HasAuthorize(PermissionConstants.SYSTEM_USER_DELETE)
    public TableBatchProcessResponse deleteUser(@Validated @RequestBody TableBatchProcessDTO request) {
        return userService.deleteUser(request, SessionUtils.getCurrentUserId(), SessionUtils.getCurrentUser().getUsername());
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-用户-修改用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = UserLogService.class)
    public UserEditRequest update(@Validated({Updated.class}) @RequestBody UserEditRequest request) {
        return userService.updateUser(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update/enable")
    @Operation(summary = "系统设置-系统-用户-启用/禁用用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.batchUpdateEnableLog(#request)", logClass = UserLogService.class)
    public TableBatchProcessResponse updateUserEnable(@Validated @RequestBody UserChangeEnableRequest request) {
        return userService.updateUserEnable(request, SessionUtils.getCurrentUserId(), SessionUtils.getCurrentUser().getUsername());
    }

    @PostMapping("/reset/password")
    @Operation(summary = "系统设置-系统-用户-重置用户密码")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.resetPasswordLog(#request)", logClass = UserLogService.class)
    public TableBatchProcessResponse resetPassword(@Validated @RequestBody TableBatchProcessDTO request) {
        return userService.resetPassword(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/get/{keyword}")
    @Operation(summary = "通过email或id查找用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_READ)
    public UserDTO getInfo(@PathVariable String keyword) {
        return userService.getUserByKeyword(keyword);
    }

    @PostMapping("/page")
    @Operation(summary = "系统设置-系统-用户-分页查找用户")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_READ)
    public Page<UserTableResponse> page(@Validated @RequestBody BasePageRequest request) {
        return userService.page(request);
    }

    @GetMapping("/get/global/system/role")
    @Operation(summary = "系统设置-系统-用户-查找系统级用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_READ)
    public List<UserSelectOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }

    @GetMapping("/get/organization")
    @Operation(summary = "系统设置-系统-用户-用户批量操作-查找组织")
    @HasAnyAuthorize(value = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.AND)
    public List<OptionDTO> getOrganization() {
        return organizationService.listOptionAll();
    }

    @PostMapping("/add/batch/user-role")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到多个用户组中")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_UPDATE)
    public TableBatchProcessResponse batchAddUserGroupRole(@Validated({Created.class}) @RequestBody UserRoleBatchRelationRequest request) {
        TableBatchProcessResponse returnResponse = globalUserRoleRelationService.batchAdd(request, SessionUtils.getCurrentUserId());
        userLogService.batchAddUserRoleLog(request, SessionUtils.getCurrentUserId());
        return returnResponse;
    }

    @PostMapping("/add-org-member")
    @Operation(summary = "系统设置-系统-用户-批量添加用户到组织")
    @HasAnyAuthorize(value = {PermissionConstants.SYSTEM_USER_UPDATE, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD}, logical = Logical.AND)
    public TableBatchProcessResponse addMember(@Validated @RequestBody UserRoleBatchRelationRequest userRoleBatchRelationRequest) {
        //获取本次处理的用户
        userRoleBatchRelationRequest.setSelectIds(userToolService.getBatchUserIds(userRoleBatchRelationRequest));
        OrganizationMemberBatchRequest request = new OrganizationMemberBatchRequest();
        request.setOrganizationIds(userRoleBatchRelationRequest.getRoleIds());
        request.setUserIds(userRoleBatchRelationRequest.getSelectIds());
        organizationService.addMemberBySystem(request, SessionUtils.getCurrentUserId());
        userLogService.batchAddOrgLog(userRoleBatchRelationRequest, SessionUtils.getCurrentUserId());
        return new TableBatchProcessResponse(userRoleBatchRelationRequest.getSelectIds().size(), userRoleBatchRelationRequest.getSelectIds().size());
    }
}
