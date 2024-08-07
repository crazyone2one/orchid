package cn.master.backend.service;

import cn.master.backend.payload.dto.user.UserRolePermissionDTO;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserRolePermission;

/**
 * 用户组权限 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface UserRolePermissionService extends IService<UserRolePermission> {
    UserRolePermissionDTO getUserRolePermission(String userId);
}
