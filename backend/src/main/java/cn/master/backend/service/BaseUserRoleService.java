package cn.master.backend.service;

import cn.master.backend.entity.User;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户组 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface BaseUserRoleService extends IService<UserRole> {

    UserRole add(UserRole userRole);

    UserRole checkResourceExist(UserRole userRole);

    UserRole getWithCheck(String id);

    void checkGlobalUserRole(UserRole userRole);

    void delete(UserRole userRole, String defaultRoleId, String currentUserId, String orgId);

    void checkNewRoleExist(UserRole userRole);

    UserRole update(UserRole userRole);

    List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    void checkMemberParam(String userId, String roleId);

    UserRole get(String id);

    List<UserExtendDTO> getMember(String sourceId, String roleId, String keyword);
}
