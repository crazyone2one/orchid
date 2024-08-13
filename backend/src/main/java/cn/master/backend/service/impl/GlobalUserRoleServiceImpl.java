package cn.master.backend.service.impl;

import cn.master.backend.constants.UserRoleScope;
import cn.master.backend.constants.UserRoleType;
import cn.master.backend.entity.UserRole;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.PermissionCache;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.BaseUserRoleRelationService;
import cn.master.backend.service.GlobalUserRoleService;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.master.backend.constants.InternalUserRole.MEMBER;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.handler.result.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;
import static cn.master.backend.handler.result.SystemResultCode.GLOBAL_USER_ROLE_PERMISSION;
import static cn.master.backend.handler.result.SystemResultCode.GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Service
public class GlobalUserRoleServiceImpl extends BaseUserRoleServiceImpl implements GlobalUserRoleService {


    public GlobalUserRoleServiceImpl(BaseUserRolePermissionService baseUserRolePermissionService,
                                     BaseUserRoleRelationService baseUserRoleRelationService,
                                     PermissionCache permissionCache) {
        super(baseUserRolePermissionService, baseUserRoleRelationService, permissionCache);
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setScopeId(UserRoleScope.GLOBAL);
        checkExist(userRole);
        return super.add(userRole);
    }

    @Override
    public void checkGlobalUserRole(UserRole userRole) {
        if (!StringUtils.equals(userRole.getScopeId(), UserRoleScope.GLOBAL)) {
            throw new MSException(GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    public void delete(String id, String currentUserId) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        super.delete(userRole, MEMBER.getValue(), currentUserId, UserRoleScope.SYSTEM);
    }

    @Override
    public List<UserRole> getRoleList() {
        val userRoles = queryChain().where(UserRole::getScopeId).eq(UserRoleScope.GLOBAL).list();
        userRoles.sort(Comparator.comparingInt(this::getTypeOrder)
                .thenComparingInt(item -> getInternal(item.getInternal()))
                .thenComparing(UserRole::getCreateTime));
        return userRoles;
    }

    @Override
    public List<UserRole> getList(List<String> idList) {
        return idList.isEmpty() ? List.of() : queryChain().where(USER_ROLE.ID.in(idList)).list();
    }

    @Override
    public void checkSystemUserGroup(UserRole userRole) {
        if (!StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleType.SYSTEM.name())) {
            throw new MSException(GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION);
        }
    }

    private int getInternal(Boolean internal) {
        return BooleanUtils.isTrue(internal) ? 0 : 1;
    }

    private int getTypeOrder(UserRole userRole) {
        Map<String, Integer> typeOrderMap = new HashMap<>(3) {{
            put(UserRoleType.SYSTEM.name(), 1);
            put(UserRoleType.ORGANIZATION.name(), 2);
            put(UserRoleType.PROJECT.name(), 3);
        }};
        return typeOrderMap.getOrDefault(userRole.getType(), 0);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole originUserRole = getWithCheck(userRole.getId());
        checkGlobalUserRole(originUserRole);
        checkInternalUserRole(originUserRole);
        userRole.setInternal(false);
        checkExist(userRole);
        mapper.update(userRole);
        return userRole;
    }

    private void checkInternalUserRole(UserRole userRole) {
        if (BooleanUtils.isTrue(userRole.getInternal())) {
            throw new MSException(INTERNAL_USER_ROLE_PERMISSION);
        }
    }

    private void checkExist(UserRole userRole) {
        if (StringUtils.isBlank(userRole.getName())) {
            return;
        }
        queryChain().where(USER_ROLE.NAME.eq(userRole.getName())
                        .and(USER_ROLE.SCOPE_ID.eq(UserRoleScope.GLOBAL)))
                .and(USER_ROLE.ID.ne(userRole.getId()));
    }
}
