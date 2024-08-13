package cn.master.backend.service;

import cn.master.backend.payload.dto.user.UserRolePermissionDTO;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserRolePermission;

import java.util.List;
import java.util.Set;

/**
 * 用户组权限 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface BaseUserRolePermissionService extends IService<UserRolePermission> {
    UserRolePermissionDTO getUserRolePermission(String userId);

    List<UserRolePermission> getPermissions(List<String> strings);

    void deleteByRoleId(String roleId);

    Set<String> getPermissionIdSetByRoleId(String roleId);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);
}
