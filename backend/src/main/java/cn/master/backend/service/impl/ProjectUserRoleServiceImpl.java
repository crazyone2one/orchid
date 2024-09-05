package cn.master.backend.service.impl;

import cn.master.backend.constants.InternalUserRole;
import cn.master.backend.constants.UserRoleType;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.PermissionCache;
import cn.master.backend.payload.dto.project.ProjectUserRoleDTO;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberEditRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.BaseUserRoleRelationService;
import cn.master.backend.service.ProjectUserRoleService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;
import static cn.master.backend.handler.result.SystemResultCode.NO_PROJECT_USER_ROLE_PERMISSION;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Service
public class ProjectUserRoleServiceImpl extends BaseUserRoleServiceImpl implements ProjectUserRoleService {
    public ProjectUserRoleServiceImpl(BaseUserRolePermissionService baseUserRolePermissionService,
                                      BaseUserRoleRelationService baseUserRoleRelationService,
                                      PermissionCache permissionCache) {
        super(baseUserRolePermissionService, baseUserRoleRelationService, permissionCache);
    }

    @Override
    public Page<ProjectUserRoleDTO> listPage(ProjectUserRoleRequest request) {
        Page<ProjectUserRoleDTO> page = QueryChain.of(UserRole.class).select(USER_ROLE.ALL_COLUMNS).from(USER_ROLE)
                .where(USER_ROLE.TYPE.eq("PROJECT").and(USER_ROLE.SCOPE_ID.in(request.getProjectId(), "global")))
                .and(USER_ROLE.ID.like(request.getKeyword())
                        .or(USER_ROLE.NAME.like(request.getKeyword())))
                .orderBy(USER_ROLE.INTERNAL.desc(), USER_ROLE.SCOPE_ID.desc(), USER_ROLE.CREATE_TIME.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), ProjectUserRoleDTO.class);
        List<String> roleIds = page.getRecords().stream().map(ProjectUserRoleDTO::getId).toList();
        List<UserRoleRelation> relations = getRelationByRoleIds(request.getProjectId(), roleIds);
        if (CollectionUtils.isNotEmpty(relations)) {
            Map<String, Long> countMap = relations.stream().collect(Collectors.groupingBy(UserRoleRelation::getRoleId, Collectors.counting()));
            page.getRecords().forEach(role -> {
                if (countMap.containsKey(role.getId())) {
                    role.setMemberCount(countMap.get(role.getId()).intValue());
                } else {
                    role.setMemberCount(0);
                }
            });
        } else {
            page.getRecords().forEach(role -> role.setMemberCount(0));
        }
        return page;
    }

    private List<UserRoleRelation> getRelationByRoleIds(String projectId, List<String> roleIds) {
        return QueryChain.of(UserRoleRelation.class)
                .select(USER_ROLE_RELATION.ALL_COLUMNS).from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER_ROLE_RELATION.ROLE_ID.in(roleIds))
                .list();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setType(UserRoleType.PROJECT.name());
        checkNewRoleExist(userRole);
        return super.add(userRole);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserRole update(UserRole userRole) {
        UserRole oldRole = get(userRole.getId());
        // 非项目用户组, 全局用户组不允许修改
        checkProjectUserRole(oldRole);
        checkGlobalUserRole(oldRole);
        userRole.setType(UserRoleType.PROJECT.name());
        checkNewRoleExist(userRole);
        return super.update(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = get(request.getUserRoleId());
        checkProjectUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    @Override
    public Page<User> listMember(ProjectUserRoleMemberRequest request) {
        return QueryChain.of(User.class)
                .select(USER.ALL_COLUMNS).from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()).and(USER_ROLE_RELATION.ROLE_ID.eq(request.getUserRoleId())))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc())
                .page(Page.of(request.getCurrent(), request.getPageSize()));
    }

    @Override
    public void addMember(ProjectUserRoleMemberEditRequest request, String createUserId) {
        Project project = QueryChain.of(Project.class).where(Project::getId).eq(request.getProjectId()).one();
        request.getUserIds().forEach(userId -> {
            checkMemberParam(userId, request.getUserRoleId());
            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(request.getUserRoleId());
            relation.setSourceId(request.getProjectId());
            relation.setCreateUser(createUserId);
            relation.setOrganizationId(project.getOrganizationId());
            super.baseUserRoleRelationService.save(relation);
        });
    }

    @Override
    public void removeMember(ProjectUserRoleMemberEditRequest request) {
        String removeUserId = request.getUserIds().getFirst();
        checkMemberParam(removeUserId, request.getUserRoleId());
        // 检查移除的是不是管理员
        if (StringUtils.equals(request.getUserRoleId(), InternalUserRole.PROJECT_ADMIN.getValue())) {
            boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.ne(removeUserId)
                    .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).exists();
            if (!exists) {
                throw new MSException(Translator.get("keep_at_least_one_administrator"));
            }
        }
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER_ROLE_RELATION.ROLE_ID.ne(request.getUserRoleId()))).exists();
        if (!exists) {
            throw new MSException(Translator.get("org_at_least_one_user_role_require"));
        }
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(removeUserId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(request.getUserRoleId())));
        LogicDeleteManager.execWithoutLogicDelete(() -> super.baseUserRoleRelationService.remove(queryChain));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id, String userId) {
        UserRole userRole = get(id);
        // 非项目用户组不允许删除, 内置用户组不允许删除
        checkProjectUserRole(userRole);
        checkGlobalUserRole(userRole);
        super.delete(userRole, InternalUserRole.PROJECT_MEMBER.getValue(), userId, userRole.getScopeId());
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(String id) {
        UserRole userRole = get(id);
        checkProjectUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    private void checkProjectUserRole(UserRole userRole) {
        if (!UserRoleType.PROJECT.name().equals(userRole.getType())) {
            throw new MSException(NO_PROJECT_USER_ROLE_PERMISSION);
        }
    }
}
