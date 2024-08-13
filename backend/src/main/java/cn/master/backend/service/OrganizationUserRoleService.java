package cn.master.backend.service;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberEditRequest;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface OrganizationUserRoleService extends BaseUserRoleService {
    List<UserRole> list(String organizationId);

    @Override
    UserRole add(UserRole userRole);

    @Override
    UserRole update(UserRole userRole);

    void delete(String roleId, String currentUserId);

    List<PermissionDefinitionItem> getPermissionSetting(String id);

    @Override
    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    Page<User> listMember(OrganizationUserRoleMemberRequest request);

    List<UserExtendDTO> getMember(String sourceId, String roleId, String keyword);

    void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId);

    void removeMember(OrganizationUserRoleMemberEditRequest request);
}
