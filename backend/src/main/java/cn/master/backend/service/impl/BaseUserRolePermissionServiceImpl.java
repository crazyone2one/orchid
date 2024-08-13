package cn.master.backend.service.impl;

import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRolePermission;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.mapper.UserRolePermissionMapper;
import cn.master.backend.payload.dto.user.UserRolePermissionDTO;
import cn.master.backend.payload.dto.user.UserRoleResourceDTO;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.BaseUserRolePermissionService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户组权限 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Service
public class BaseUserRolePermissionServiceImpl extends ServiceImpl<UserRolePermissionMapper, UserRolePermission> implements BaseUserRolePermissionService {

    @Override
    public UserRolePermissionDTO getUserRolePermission(String userId) {
        UserRolePermissionDTO permissionDTO = new UserRolePermissionDTO();
        List<UserRoleResourceDTO> list = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(UserRoleRelation::getUserId).eq(userId).list();
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return permissionDTO;
        }
        permissionDTO.setUserRoleRelations(userRoleRelations);
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(UserRole::getId).in(roleList).list();
        permissionDTO.setUserRoles(userRoles);
        for (UserRole userRole : userRoles) {
            UserRoleResourceDTO dto = new UserRoleResourceDTO();
            dto.setUserRole(userRole);
            List<UserRolePermission> userRolePermissions = QueryChain.of(UserRolePermission.class).where(UserRolePermission::getRoleId).eq(userRole.getId()).list();
            dto.setUserRolePermissions(userRolePermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }

    @Override
    public List<UserRolePermission> getPermissions(List<String> strings) {
        return queryChain().where(UserRolePermission::getRoleId).in(strings).list();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByRoleId(String roleId) {
        val queryChain = queryChain().where(UserRolePermission::getRoleId).eq(roleId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public Set<String> getPermissionIdSetByRoleId(String roleId) {
        return queryChain().where(UserRolePermission::getRoleId).eq(roleId).list()
                .stream()
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        List<PermissionSettingUpdateRequest.PermissionUpdateRequest> permissions = request.getPermissions();
        val queryChain = queryChain().where(UserRolePermission::getRoleId).eq(request.getUserRoleId())
                .and(UserRolePermission::getPermissionId).ne("PROJECT_BASE_INFO:READ");
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
        String groupId = request.getUserRoleId();
        permissions.forEach(permission -> {
            if (BooleanUtils.isTrue(permission.getEnable())) {
                String permissionId = permission.getId();
                UserRolePermission groupPermission = new UserRolePermission();
                groupPermission.setRoleId(groupId);
                groupPermission.setPermissionId(permissionId);
                mapper.insert(groupPermission);
            }
        });
    }
}
