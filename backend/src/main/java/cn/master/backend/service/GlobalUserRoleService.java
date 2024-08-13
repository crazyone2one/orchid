package cn.master.backend.service;

import cn.master.backend.entity.UserRole;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface GlobalUserRoleService extends BaseUserRoleService {
    @Override
    UserRole add(UserRole userRole);

    UserRole update(UserRole userRole);

    @Override
    void checkGlobalUserRole(UserRole userRole);

    void delete(String id, String currentUserId);

    List<UserRole> getRoleList();

    List<UserRole> getList(List<String> idList);

    void checkSystemUserGroup(UserRole userRole);
}
