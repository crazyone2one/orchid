package cn.master.backend.service;

import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.payload.response.user.UserSelectOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface GlobalUserRoleService extends BaseUserRoleService {
    @Override
    UserRole add(UserRole userRole);

    @Override
    UserRole update(UserRole userRole);

    @Override
    void checkGlobalUserRole(UserRole userRole);

    void delete(String id, String currentUserId);

    List<UserRole> getRoleList();

    List<UserRole> getList(List<String> idList);

    void checkSystemUserGroup(UserRole userRole);

    void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> userRoleIdList, boolean isSystem);

    List<UserRole> selectByUserRoleRelations(List<UserRoleRelation> userRoleRelations);

    List<UserSelectOption> getGlobalSystemRoleList();
}
