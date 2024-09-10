package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.project.ProjectUserDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.user.CommentUserInfo;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.project.ProjectMemberAddRequest;
import cn.master.backend.payload.request.project.ProjectMemberAddRoleRequest;
import cn.master.backend.payload.request.project.ProjectMemberBatchDeleteRequest;
import cn.master.backend.payload.request.project.ProjectMemberEditRequest;
import cn.master.backend.payload.request.system.ProjectMemberRequest;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.ProjectMemberService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    private final OperationLogService operationLogService;
    private final UserRoleRelationMapper userRoleRelationMapper;

    @Override
    public Page<ProjectUserDTO> listMember(ProjectMemberRequest request) {
        // 查询当前项目成员
        List<String> members = listMemberIds(request);
        if (CollectionUtils.isEmpty(members)) {
            return new Page<>();
        }
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                .and(USER_ROLE_RELATION.USER_ID.in(members))).list();
        userRoleRelations.sort(Comparator.comparing(UserRoleRelation::getCreateTime).reversed());
        Map<String, List<String>> userRoleRelateMap = userRoleRelations.stream().collect(Collectors.groupingBy(UserRoleRelation::getUserId,
                Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        List<UserRole> roles = QueryChain.of(UserRole.class).where(USER_ROLE.TYPE.eq(UserRoleType.PROJECT.name())).list();
        Map<String, UserRole> roleMap = roles.stream().collect(Collectors.toMap(UserRole::getId, role -> role));
        List<ProjectUserDTO> projectUsers = new ArrayList<>();
        userRoleRelateMap.forEach((k, v) -> {
            ProjectUserDTO projectUser = new ProjectUserDTO();
            projectUser.setId(k);
            List<UserRole> userRoles = new ArrayList<>();
            v.forEach(roleId -> {
                UserRole role = roleMap.get(roleId);
                userRoles.add(role);
            });
            projectUser.setUserRoles(userRoles);
            projectUsers.add(projectUser);
        });
        // 设置用户信息
        List<String> uerIds = projectUsers.stream().map(ProjectUserDTO::getId).toList();
        List<User> users = QueryChain.of(User.class).where(User::getId).in(uerIds).list();
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        projectUsers.forEach(projectUser -> {
            User user = userMap.get(projectUser.getId());
            BeanUtils.copyProperties(user, projectUser);
        });
        return new Page<>(projectUsers, request.getCurrent(), request.getPageSize(), projectUsers.size());
    }

    @Override
    public List<UserExtendDTO> getMemberOption(String projectId, String keyword) {
        Project project = QueryChain.of(Project.class).where(Project::getId).eq(projectId).one();
        if (Objects.isNull(project)) {
            return List.of();
        }
        // 组织成员
        List<UserExtendDTO> orgMembers = getMemberByOrg(project.getOrganizationId(), keyword);
        if (orgMembers.isEmpty()) {
            return List.of();
        }
        // 设置是否是项目成员
        List<String> orgMemberIds = orgMembers.stream().map(UserExtendDTO::getId).toList();
        List<UserRoleRelation> projectRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.in(orgMemberIds)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER_ROLE_RELATION.ORGANIZATION_ID.eq(project.getOrganizationId()))).list();
        if (CollectionUtils.isEmpty(projectRelations)) {
            orgMembers.forEach(orgMember -> orgMember.setMemberFlag(false));
        } else {
            List<String> projectUsers = projectRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
            // 已经是项目成员的组织成员, 禁用
            orgMembers.forEach(orgMember -> orgMember.setMemberFlag(projectUsers.contains(orgMember.getId())));
        }
        return List.of();
    }

    @Override
    public List<OptionDTO> getRoleOption(String projectId) {
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.TYPE.eq(UserRoleType.PROJECT.name())
                .and(USER_ROLE.SCOPE_ID.in(Arrays.asList(projectId, UserRoleEnum.GLOBAL.toString())))).list();
        return userRoles.stream().map(userRole -> new OptionDTO(userRole.getId(), userRole.getName())).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addMember(ProjectMemberAddRequest request, String currentUserId) {
        ProjectMemberAddRoleRequest roleRequest = new ProjectMemberAddRoleRequest();
        roleRequest.setProjectId(request.getProjectId());
        roleRequest.setRoleIds(request.getRoleIds());
        roleRequest.setSelectAll(false);
        roleRequest.setSelectIds(request.getUserIds());
        addMemberRole(roleRequest, currentUserId, OperationLogType.ADD.name(), "/project/member/add");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateMember(ProjectMemberEditRequest request, String currentUserId) {
// 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在
        Project project = checkProjectExist(request.getProjectId());
        // 移除已经存在的用户组
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                .and(USER_ROLE_RELATION.USER_ID.eq(request.getUserId())));
        // 旧的用户组关系, 操作记录使用
        List<UserRoleRelation> oldRelations = userRoleRelationMapper.selectListByQuery(queryChain);
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        // 添加新的用户组
        List<UserRoleRelation> relations = new ArrayList<>();
        request.getRoleIds().forEach(roleId -> {
            // 用户不存在或用户组不存在, 则不添加
            if (isUserOrRoleNotExist(request.getUserId(), roleId)) {
                return;
            }
            UserRoleRelation relation = new UserRoleRelation();
            relation.setUserId(request.getUserId());
            relation.setRoleId(roleId);
            relation.setSourceId(request.getProjectId());
            relation.setCreateUser(currentUserId);
            relation.setOrganizationId(project.getOrganizationId());
            relations.add(relation);
        });
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.insertBatch(relations);
        }
        List<UserRole> newRoles = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(request.getRoleIds())).list();
        List<UserRole> oldRoles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(oldRelations)) {
            List<String> oldRoleIds = oldRelations.stream().map(UserRoleRelation::getRoleId).toList();
            oldRoles = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(oldRoleIds)).list();
        }
        setLog(request.getProjectId(), request.getUserId(), currentUserId, OperationLogType.UPDATE.name(), "/project/member/update", HttpMethodConstants.POST.name(), oldRoles, newRoles, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public void removeMember(String projectId, String userId, String currentUserId) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不移除
        checkProjectExist(projectId);
        //判断用户是不是最后一个管理员  如果是  就报错
        long count = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.ne(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).count();
        if (count == 0) {
            throw new MSException(Translator.get("keep_at_least_one_administrator"));
        }
        // 移除成员, 则移除该成员在该项目下的所有用户组
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)
                .and(USER_ROLE_RELATION.USER_ID.eq(userId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        // 操作记录
        setLog(projectId, userId, currentUserId, OperationLogType.DELETE.name(), "/project/member/remove", HttpMethodConstants.GET.name(), null, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public void addRole(ProjectMemberAddRoleRequest request, String currentUserId) {
        addMemberRole(request, currentUserId, OperationLogType.UPDATE.name(), "/project/member/add-role");
    }

    @Override
    public void batchRemove(ProjectMemberBatchDeleteRequest request, String currentUserId) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不移除
        checkProjectExist(request.getProjectId());
        if (!request.isSelectAll() && CollectionUtils.isEmpty(request.getSelectIds())) {
            throw new MSException(Translator.get("user.not.empty"));
        }
        // 批量移除成员, 则移除该成员在该项目下的所有用户组
        List<String> userIds;
        if (request.isSelectAll()) {
            userIds = getProjectRoleMemberIds(request);
            if (!CollectionUtils.isEmpty(request.getExcludeIds())) {
                userIds.removeAll(request.getExcludeIds());
            }
        } else {
            userIds = request.getSelectIds();
        }
        // 移除成员, 则移除该成员在该项目下的所有用户组
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                .and(USER_ROLE_RELATION.USER_ID.in(userIds)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        // 操作记录
        userIds.forEach(userId -> {
            // 操作记录
            setLog(request.getProjectId(), userId, currentUserId, OperationLogType.DELETE.name(), "/project/member/remove", HttpMethodConstants.GET.name(), null, null, logs);
        });
        operationLogService.batchAdd(logs);
    }

    @Override
    public List<CommentUserInfo> selectCommentUser(String projectId, String keyword) {
        return QueryChain.of(User.class).select(QueryMethods.distinct(USER.ALL_COLUMNS))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER.NAME.like(keyword))
                .listAs(CommentUserInfo.class);
    }

    private List<String> getProjectRoleMemberIds(ProjectMemberBatchDeleteRequest request) {
        return QueryChain.of(User.class)
                .select(QueryMethods.distinct(USER.ID))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER.NAME.like(request.getCondition().getKeyword())
                        .or(USER.EMAIL.like(request.getCondition().getKeyword()))
                        .or(USER.PHONE.like(request.getCondition().getKeyword())))
                .listAs(String.class);
    }

    private void addMemberRole(ProjectMemberAddRoleRequest request, String currentUserId, String operationType, String path) {
        // 操作记录
        List<LogDTO> logs = new ArrayList<>();
        // 项目不存在, 则不添加
        Project project = checkProjectExist(request.getProjectId());
        if (!request.isSelectAll() && CollectionUtils.isEmpty(request.getSelectIds())) {
            throw new MSException(Translator.get("user.not.empty"));
        }
        // 批量移除成员, 则移除该成员在该项目下的所有用户组
        List<String> userIds;
        if (request.isSelectAll()) {
            userIds = getProjectRoleMemberIds(request);
            if (!CollectionUtils.isEmpty(request.getExcludeIds())) {
                userIds.removeAll(request.getExcludeIds());
            }
        } else {
            userIds = request.getSelectIds();
        }
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                .and(USER_ROLE_RELATION.USER_ID.in(userIds))
                .and(USER_ROLE_RELATION.ROLE_ID.in(request.getRoleIds()))).list();
        Map<String, List<String>> existUserRelations = userRoleRelations.stream().collect(
                Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        // 比较用户组是否已经存在, 如果不存在则添加
        List<UserRoleRelation> relations = new ArrayList<>();
        userIds.forEach(userId -> {
            AtomicBoolean isLog = new AtomicBoolean(false);
            // 追加的用户组ID, 操作记录使用
            List<String> roleIds = new ArrayList<>();
            request.getRoleIds().forEach(roleId -> {
                // 用户不存在或用户组不存在, 则不添加
                if (isUserOrRoleNotExist(userId, roleId)) {
                    return;
                }
                // 如果该用户已经添加至该用户组, 则不再添加
                if (existUserRelations.containsKey(userId) && existUserRelations.get(userId).contains(roleId)) {
                    return;
                }
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relation.setSourceId(request.getProjectId());
                relation.setCreateUser(currentUserId);
                relation.setOrganizationId(project.getOrganizationId());
                relations.add(relation);
                isLog.set(true);
                roleIds.add(roleId);
            });
            if (isLog.get()) {
                List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(roleIds)).list();
                // 追加了哪些用户组
                setLog(request.getProjectId(), userId, currentUserId, operationType, path, HttpMethodConstants.POST.name(), null, userRoles, logs);
            }
        });
        if (!CollectionUtils.isEmpty(relations)) {
            userRoleRelationMapper.insertBatch(relations);
        }
        // 操作记录
        operationLogService.batchAdd(logs);
    }

    private void setLog(String projectId, String memberId, String createUserId, String type, String path, String method, Object originalVal, Object modifiedVal, List<LogDTO> logs) {
        Project project = QueryChain.of(Project.class).where(Project::getId).eq(projectId).one();
        User user = QueryChain.of(User.class).where(User::getId).eq(memberId).one();
        LogDTO dto = new LogDTO(
                projectId,
                project.getOrganizationId(),
                memberId,
                createUserId,
                type,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                user.getName());
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(originalVal));
        dto.setModifiedValue(JSON.toJSONBytes(modifiedVal));
        logs.add(dto);
    }

    private boolean isUserOrRoleNotExist(String userId, String roleId) {
        return QueryChain.of(User.class).where(User::getId).eq(userId).one() == null
                ||
                QueryChain.of(UserRole.class).where(UserRole::getId).eq(roleId).one() == null;
    }

    private List<String> getProjectRoleMemberIds(ProjectMemberAddRoleRequest request) {
        return QueryChain.of(User.class)
                .select(QueryMethods.distinct(USER.ID))
                .from(USER).leftJoin(USER_ROLE_RELATION).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER.NAME.like(request.getCondition().getKeyword())
                        .or(USER.EMAIL.like(request.getCondition().getKeyword()))
                        .or(USER.PHONE.like(request.getCondition().getKeyword())))
                .listAs(String.class);
    }

    private Project checkProjectExist(String projectId) {
        return QueryChain.of(Project.class).where(Project::getId).eq(projectId).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("project_not_exist")));
    }

    private List<UserExtendDTO> getMemberByOrg(String organizationId, String keyword) {
        return QueryChain.of(User.class).select(QueryMethods.distinct(USER.ALL_COLUMNS))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId))
                .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword)))
                .orderBy(String.valueOf(USER.NAME)).limit(1000)
                .listAs(UserExtendDTO.class);
    }

    private List<String> listMemberIds(ProjectMemberRequest request) {
        QueryWrapper wrapper = new QueryWrapper().select(USER.ID, USER_ROLE_RELATION.CREATE_TIME)
                .from(USER_ROLE_RELATION)
                .join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER.NAME.like(request.getKeyword()).or(USER.EMAIL.like(request.getKeyword())).or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        return QueryChain.of(UserRoleRelation.class)
                .select("distinct temp.id")
                .from(new SelectQueryTable(wrapper).as("temp"))
                .listAs(String.class);
    }
}
