package cn.master.backend.service;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.payload.dto.project.ProjectUserRoleDTO;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberEditRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
public interface ProjectUserRoleService extends BaseUserRoleService{
    Page<ProjectUserRoleDTO> listPage(ProjectUserRoleRequest request);

    @Override
    UserRole add(UserRole userRole);

    @Override
    UserRole update(UserRole userRole);

    void delete(String id, String userId);

    List<PermissionDefinitionItem> getPermissionSetting(String id);

    @Override
    void updatePermissionSetting(PermissionSettingUpdateRequest request);

    Page<User> listMember(ProjectUserRoleMemberRequest request);

    void addMember(ProjectUserRoleMemberEditRequest request, String userId);

    void removeMember(ProjectUserRoleMemberEditRequest request);
}
