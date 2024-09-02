package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.UserRole;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.system.request.UserRoleUpdateRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.GlobalUserRoleService;
import cn.master.backend.service.log.GlobalUserRoleLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-用户组")
@RequestMapping("/user/role/global")
public class GlobalUserRoleController {

    private final GlobalUserRoleService globalUserRoleService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-用户组-添加自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_ADD)
    public UserRole save(@Validated({Created.class}) @RequestBody UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getCurrentUserId());
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.add(userRole);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-用户组-删除自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_DELETE)
    public void remove(@PathVariable String id) {
        globalUserRoleService.delete(id, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-用户组-更新自定义全局用户组")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    public UserRole update(@Validated({Updated.class}) @RequestBody UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.update(userRole);
    }

    /**
     * 查询所有用户组。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(summary = "系统设置-系统-用户组-获取全局用户组列表")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<UserRole> list() {
        return globalUserRoleService.getRoleList();
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "系统设置-系统-用户组-获取全局用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_READ)
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return globalUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "系统设置-系统-用户组-编辑全局用户组对应的权限配置")
    @HasAuthorize(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = GlobalUserRoleLogService.class)
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        globalUserRoleService.updatePermissionSetting(request);
    }
}
