package cn.master.backend.service.impl;

import cn.master.backend.constants.UserRoleEnum;
import cn.master.backend.constants.UserRoleType;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRolePermission;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.UserRoleMapper;
import cn.master.backend.payload.PermissionCache;
import cn.master.backend.payload.dto.system.Permission;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.BaseUserRoleRelationService;
import cn.master.backend.service.BaseUserRoleService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.handler.result.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;
import static cn.master.backend.handler.result.SystemResultCode.NO_GLOBAL_USER_ROLE_PERMISSION;

/**
 * 用户组 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Service("baseUserRoleService")
@RequiredArgsConstructor
public class BaseUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements BaseUserRoleService {
    private final BaseUserRolePermissionService baseUserRolePermissionService;
    protected final BaseUserRoleRelationService baseUserRoleRelationService;
    private final PermissionCache permissionCache;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserRole add(UserRole userRole) {
        mapper.insert(userRole);
        if (StringUtils.equals(userRole.getType(), UserRoleType.PROJECT.name())) {
            // 项目级别用户组, 初始化基本信息权限
            UserRolePermission initPermission = new UserRolePermission();
            initPermission.setRoleId(userRole.getId());
            initPermission.setPermissionId("PROJECT_BASE_INFO:READ");
            baseUserRolePermissionService.save(initPermission);
        }
        return userRole;
    }

    @Override
    public UserRole checkResourceExist(UserRole userRole) {
        return ServiceUtils.checkResourceExist(userRole, "permission.system_user_role.name");
    }

    @Override
    public UserRole getWithCheck(String id) {
        return checkResourceExist(mapper.selectOneById(id));
    }

    @Override
    public void checkGlobalUserRole(UserRole userRole) {
        if (StringUtils.equals(userRole.getScopeId(), UserRoleEnum.GLOBAL.toString())) {
            throw new MSException(NO_GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(UserRole userRole, String defaultRoleId, String currentUserId, String orgId) {
        String id = userRole.getId();
        checkInternalUserRole(userRole);
        baseUserRolePermissionService.deleteByRoleId(id);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
        // 检查是否只有一个用户组，如果是则添加系统成员等默认用户组
        checkOneLimitRole(id, defaultRoleId, currentUserId, orgId);
        baseUserRoleRelationService.deleteByRoleId(id);
    }

    @Override
    public void checkNewRoleExist(UserRole userRole) {
        boolean exists = queryChain().where(USER_ROLE.NAME.eq(userRole.getName())
                        .and(USER_ROLE.SCOPE_ID.in(Arrays.asList(userRole.getScopeId(), UserRoleEnum.GLOBAL.toString())))
                        .and(USER_ROLE.TYPE.eq(userRole.getType())))
                .and(USER_ROLE.ID.ne(userRole.getId())).exists();
        if (exists) {
            throw new MSException(Translator.get("user_role_exist"));
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserRole update(UserRole userRole) {
        mapper.update(userRole);
        return userRole;
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole) {
        // 获取该用户组拥有的权限
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(userRole.getId());
        // 获取所有的权限
        List<PermissionDefinitionItem> permissionDefinition = permissionCache.getPermissionDefinition();
        // 深拷贝
        permissionDefinition = JSON.parseArray(JSON.toJSONString(permissionDefinition), PermissionDefinitionItem.class);

        // 过滤该用户组级别的菜单，例如系统级别
        permissionDefinition = permissionDefinition.stream()
                .filter(item -> StringUtils.equals(item.getType(), userRole.getType()))
                .toList();
        // 设置勾选项
        for (PermissionDefinitionItem firstLevel : permissionDefinition) {
            List<PermissionDefinitionItem> children = firstLevel.getChildren();
            boolean allCheck = true;
            firstLevel.setName(Translator.get(firstLevel.getName()));
            for (PermissionDefinitionItem secondLevel : children) {
                List<Permission> permissions = secondLevel.getPermissions();
                secondLevel.setName(Translator.get(secondLevel.getName()));
                if (CollectionUtils.isEmpty(permissions)) {
                    continue;
                }
                boolean secondAllCheck = true;
                for (Permission p : permissions) {
                    if (StringUtils.isNotBlank(p.getName())) {
                        // 有 name 字段翻译 name 字段
                        p.setName(Translator.get(p.getName()));
                    } else {
                        p.setName(translateDefaultPermissionName(p));
                    }
                    if (permissionIds.contains(p.getId())) {
                        p.setEnable(true);
                    } else {
                        // 如果权限有未勾选，则二级菜单设置为未勾选
                        p.setEnable(false);
                        secondAllCheck = false;
                    }
                }
                secondLevel.setEnable(secondAllCheck);
                if (!secondAllCheck) {
                    // 如果二级菜单有未勾选，则一级菜单设置为未勾选
                    allCheck = false;
                }
            }
            firstLevel.setEnable(allCheck);
        }


        return permissionDefinition;
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        baseUserRolePermissionService.updatePermissionSetting(request);
    }

    @Override
    public void checkMemberParam(String userId, String roleId) {
        User user = QueryChain.of(User.class).where(User::getId).eq(userId).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user_not_exist")));
        UserRole userRole = mapper.selectOneById(roleId);
        if (userRole == null) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
    }

    private String translateDefaultPermissionName(Permission p) {
        if (StringUtils.isNotBlank(p.getName())) {
            p.getName();
        }
        String[] idSplit = p.getId().split(":");
        String permissionKey = idSplit[idSplit.length - 1];
        Map<String, String> translationMap = new HashMap<>() {{
            put("READ", "permission.read");
            put("READ+ADD", "permission.add");
            put("READ+UPDATE", "permission.edit");
            put("READ+DELETE", "permission.delete");
            put("READ+IMPORT", "permission.import");
            put("READ+RECOVER", "permission.recover");
            put("READ+EXPORT", "permission.export");
            put("READ+EXECUTE", "permission.execute");
            put("READ+DEBUG", "permission.debug");
        }};
        return Translator.get(translationMap.get(permissionKey));
    }

    private void checkOneLimitRole(String roleId, String defaultRoleId, String currentUserId, String orgId) {
        // 查询要删除的用户组关联的用户ID
        List<String> userIds = baseUserRoleRelationService.getUserIdByRoleId(roleId);

        if (userIds.isEmpty()) {
            return;
        }
        // 查询用户列表与所有用户组的关联关系，并分组（UserRoleRelation 中只有 userId 和 sourceId）
        Map<String, List<UserRoleRelation>> userRoleRelationMap = baseUserRoleRelationService
                .getUserIdAndSourceIdByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(i -> i.getUserId() + i.getSourceId()));
        List<UserRoleRelation> addRelations = new ArrayList<>();
        userRoleRelationMap.forEach((groupId, relations) -> {
            // 如果当前用户组只有一个用户，并且就是要删除的用户组，则添加组织成员等默认用户组
            if (relations.size() == 1 && StringUtils.equals(relations.getFirst().getRoleId(), roleId)) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(relations.getFirst().getUserId());
                relation.setSourceId(relations.getFirst().getSourceId());
                relation.setRoleId(defaultRoleId);
                relation.setCreateUser(currentUserId);
                relation.setOrganizationId(orgId);
                addRelations.add(relation);
            }
        });
        baseUserRoleRelationService.saveBatch(addRelations);
    }

    private void checkInternalUserRole(UserRole userRole) {
        if (BooleanUtils.isTrue(userRole.getInternal())) {
            throw new MSException(INTERNAL_USER_ROLE_PERMISSION);
        }
    }
}
