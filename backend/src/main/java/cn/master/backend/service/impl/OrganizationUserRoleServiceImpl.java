package cn.master.backend.service.impl;

import cn.master.backend.constants.InternalUserRole;
import cn.master.backend.constants.UserRoleEnum;
import cn.master.backend.constants.UserRoleType;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.PermissionCache;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberEditRequest;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.BaseUserRoleRelationService;
import cn.master.backend.service.OrganizationUserRoleService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;
import static cn.master.backend.handler.result.SystemResultCode.NO_ORG_USER_ROLE_PERMISSION;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Service("organizationUserRoleService")
public class OrganizationUserRoleServiceImpl extends BaseUserRoleServiceImpl implements OrganizationUserRoleService {
    public OrganizationUserRoleServiceImpl(BaseUserRolePermissionService baseUserRolePermissionService,
                                           BaseUserRoleRelationService baseUserRoleRelationService,
                                           PermissionCache permissionCache) {
        super(baseUserRolePermissionService, baseUserRoleRelationService, permissionCache);
    }

    @Override
    public List<UserRole> list(String organizationId) {
        List<UserRole> userRoles = queryChain().where(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.name())
                        .and(USER_ROLE.SCOPE_ID.in(Arrays.asList(organizationId, UserRoleEnum.GLOBAL.toString()))))
                .orderBy(USER_ROLE.CREATE_TIME.asc()).list();
        userRoles.sort(Comparator.comparing(UserRole::getInternal).thenComparing(UserRole::getScopeId)
                .thenComparing(Comparator.comparing(UserRole::getCreateTime).thenComparing(UserRole::getId).reversed()).reversed());
        return userRoles;
    }

    @Override
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        UserRole oldRole = queryChain().where(USER_ROLE.ID.eq(userRole.getId())).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user_role_not_exist")));
        // 非组织用户组不允许修改, 全局用户组不允许修改
        checkOrgUserRole(oldRole);
        checkGlobalUserRole(oldRole);
        userRole.setType(UserRoleType.ORGANIZATION.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    @Override
    public void delete(String roleId, String currentUserId) {
        UserRole userRole = queryChain().where(USER_ROLE.ID.eq(roleId)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user_role_not_exist")));
        // 非组织用户组不允许删除, 内置全局用户组不允许删除
        checkOrgUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.delete(userRole, InternalUserRole.ORG_MEMBER.getValue(), currentUserId, userRole.getScopeId());
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = queryChain().where(USER_ROLE.ID.eq(id)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user_role_not_exist")));
        checkOrgUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = queryChain().where(USER_ROLE.ID.eq(request.getUserRoleId())).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user_role_not_exist")));
        checkOrgUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    @Override
    public Page<User> listMember(OrganizationUserRoleMemberRequest request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(USER.ALL_COLUMNS)
                .from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getUserRoleId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        return super.baseUserRoleRelationService.pageAs(Page.of(request.getCurrent(), request.getPageSize()), queryWrapper, User.class);
    }

    @Override
    public List<UserExtendDTO> getMember(String sourceId, String roleId, String keyword) {
        List<UserExtendDTO> userExtends = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(sourceId)).list();
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            Map<String, List<String>> userRoleMap = userRoleRelations.stream()
                    .collect(Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
            userRoleMap.forEach((k, v) -> {
                UserExtendDTO userExtendDTO = new UserExtendDTO();
                userExtendDTO.setId(k);
                v.forEach(roleItem -> {
                    if (StringUtils.equals(roleItem, roleId)) {
                        // 该用户已存在用户组关系, 设置为选中状态
                        userExtendDTO.setCheckRoleFlag(true);
                    }
                });
                userExtends.add(userExtendDTO);
            });
            // 设置用户信息, 用户不存在或者已删除, 则不展示
            List<String> userIds = userExtends.stream().map(UserExtendDTO::getId).toList();
            List<User> users = QueryChain.of(User.class).where(USER.ID.in(userIds)
                            .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))))
                    .limit(1000).list();
            if (CollectionUtils.isNotEmpty(users)) {
                Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
                userExtends.removeIf(userExtend -> {
                    if (userMap.containsKey(userExtend.getId())) {
                        BeanUtils.copyProperties(userMap.get(userExtend.getId()), userExtend);
                        return false;
                    }
                    return true;
                });
            } else {
                userExtends.clear();
            }
        }
        return userExtends;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addMember(OrganizationUserRoleMemberEditRequest request, String createUserId) {
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getOrganizationId());
            relation.setCreateUser(createUserId);
            relation.setOrganizationId(request.getOrganizationId());
            super.baseUserRoleRelationService.save(relation);
        });
    }

    @Override
    public void removeMember(OrganizationUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().getFirst();
        checkMemberParam(removeUserId, request.getUserRoleId());
        if (StringUtils.equals(request.getUserRoleId(), InternalUserRole.ORG_ADMIN.getValue())) {
            boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                    .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))).exists();
            if (!exists) {
                throw new MSException(Translator.get("keep_at_least_one_administrator"));
            }
        }
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                .and(USER_ROLE_RELATION.ROLE_ID.ne(request.getUserRoleId()))).exists();
        if (!exists) {
            throw new MSException(Translator.get("org_at_least_one_user_role_require"));
        }
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(request.getUserRoleId())));
        LogicDeleteManager.execWithoutLogicDelete(() -> super.baseUserRoleRelationService.remove(queryChain));
    }

    private void checkOrgUserRole(UserRole userRole) {
        if (!UserRoleType.ORGANIZATION.name().equals(userRole.getType())) {
            throw new MSException(NO_ORG_USER_ROLE_PERMISSION);
        }
    }
}
