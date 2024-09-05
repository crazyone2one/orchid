package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.mapper.ProjectTestResourcePoolMapper;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.system.ProjectResourcePoolDTO;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.dto.user.UserRolePermissionDTO;
import cn.master.backend.payload.request.project.ProjectSwitchRequest;
import cn.master.backend.payload.request.project.ProjectUpdateRequest;
import cn.master.backend.payload.request.system.*;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.ProjectService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.ProjectTestResourcePoolTableDef.PROJECT_TEST_RESOURCE_POOL;
import static cn.master.backend.entity.table.TestResourcePoolOrganizationTableDef.TEST_RESOURCE_POOL_ORGANIZATION;
import static cn.master.backend.entity.table.TestResourcePoolTableDef.TEST_RESOURCE_POOL;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 项目 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service("projectService")
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
    private final ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;
    private final BaseUserRolePermissionService baseUserRolePermissionService;
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    public static final String API_TEST = "apiTest";
    public static final String TEST_PLAN = "testPlan";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO add(AddProjectRequest request, String createUser, String path, String module) {
        Project project = new Project();
        project.setName(request.getName());
        project.setOrganizationId(request.getOrganizationId());
        checkProjectExistByName(project);
        project.setUpdateUser(createUser);
        project.setCreateUser(createUser);
        project.setEnable(request.getEnable());
        project.setDescription(request.getDescription());
        project.setModuleSetting(request.getModuleIds());
        mapper.insert(project);
        if (CollectionUtils.isNotEmpty(request.getResourcePoolIds())) {
            checkResourcePoolExist(request.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            QueryChain<ProjectTestResourcePool> where = QueryChain.of(ProjectTestResourcePool.class).where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(project.getId()));
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(where));
            request.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.insertBatch(projectTestResourcePools);
        }
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        memberRequest.setUserIds(request.getUserIds());
        addProjectAdmin(memberRequest, createUser, path, OperationLogType.ADD.name(), Translator.get("add"), module);
        return projectDTO;
    }

    @Override
    public ProjectDTO get(String id) {
        ProjectDTO projectDTO = queryChain().where(PROJECT.ID.eq(id).and(PROJECT.ENABLE.eq(true))).oneAs(ProjectDTO.class);
        List<ProjectDTO> projects = buildUserInfo(List.of(projectDTO));
        return projects.getFirst();
    }

    @Override
    public List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList) {
        List<String> projectIds = projectList.stream().map(ProjectDTO::getId).toList();
        List<UserExtendDTO> users = getProjectAdminList(projectIds);
        List<ProjectDTO> projectDTOList = getProjectExtendDTOList(projectIds);
        Map<String, ProjectDTO> projectMap = projectDTOList.stream().collect(Collectors.toMap(ProjectDTO::getId, projectDTO -> projectDTO));
        Map<String, List<UserExtendDTO>> userMapList = users.stream().collect(Collectors.groupingBy(UserExtendDTO::getSourceId));
        //获取资源池
        List<ProjectResourcePoolDTO> projectResourcePoolDTOList = getProjectResourcePoolDTOList(projectIds);
        //根据projectId分组 key为项目id 值为资源池TestResourcePool
        Map<String, List<ProjectResourcePoolDTO>> poolMap = projectResourcePoolDTOList.stream().collect(Collectors.groupingBy(ProjectResourcePoolDTO::getProjectId));
        projectList.forEach(projectDTO -> {
            if (CollectionUtils.isNotEmpty(projectDTO.getModuleSetting())) {
                projectDTO.setModuleIds(projectDTO.getModuleSetting());
            }
            projectDTO.setOrganizationName(QueryChain.of(Organization.class).where(ORGANIZATION.ID.eq(projectDTO.getOrganizationId())).one().getName());
            projectDTO.setMemberCount(projectMap.get(projectDTO.getId()).getMemberCount());
            List<UserExtendDTO> userExtends = userMapList.get(projectDTO.getId());
            if (CollectionUtils.isNotEmpty(userExtends)) {
                projectDTO.setAdminList(userExtends);
                List<String> userIdList = userExtends.stream().map(User::getId).collect(Collectors.toList());
                projectDTO.setProjectCreateUserIsAdmin(CollectionUtils.isNotEmpty(userIdList) && userIdList.contains(projectDTO.getCreateUser()));
            } else {
                projectDTO.setAdminList(new ArrayList<>());
            }
            List<ProjectResourcePoolDTO> projectResourcePools = poolMap.get(projectDTO.getId());
            if (CollectionUtils.isNotEmpty(projectResourcePools)) {
                projectDTO.setResourcePoolList(projectResourcePools);
            } else {
                projectDTO.setResourcePoolList(new ArrayList<>());
            }
        });
        return projectList;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser, String path, String module) {
        Project project = new Project();
        ProjectDTO projectDTO = new ProjectDTO();
        project.setId(updateProjectDto.getId());
        project.setName(updateProjectDto.getName());
        project.setDescription(updateProjectDto.getDescription());
        project.setOrganizationId(updateProjectDto.getOrganizationId());
        project.setEnable(updateProjectDto.getEnable());
        project.setUpdateUser(updateUser);
        project.setModuleSetting(updateProjectDto.getModuleIds());
        checkProjectExistByName(project);
        checkProjectNotExist(project.getId());
        BeanUtils.copyProperties(project, projectDTO);
        if (CollectionUtils.isNotEmpty(updateProjectDto.getResourcePoolIds())) {
            checkResourcePoolExist(updateProjectDto.getResourcePoolIds());
            List<ProjectTestResourcePool> projectTestResourcePools = new ArrayList<>();
            QueryChain<ProjectTestResourcePool> poolQueryChain = QueryChain.of(ProjectTestResourcePool.class)
                    .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(project.getId()));
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(poolQueryChain));
            updateProjectDto.getResourcePoolIds().forEach(resourcePoolId -> {
                ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
                projectTestResourcePool.setProjectId(project.getId());
                projectTestResourcePool.setTestResourcePoolId(resourcePoolId);
                projectTestResourcePools.add(projectTestResourcePool);
            });
            projectTestResourcePoolMapper.insertBatch(projectTestResourcePools);
        } else {
            QueryChain<ProjectTestResourcePool> poolQueryChain = QueryChain.of(ProjectTestResourcePool.class)
                    .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(project.getId()));
            LogicDeleteManager.execWithoutLogicDelete(() -> projectTestResourcePoolMapper.deleteByQuery(poolQueryChain));
        }
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).list();
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> deleteIds = orgUserIds.stream()
                .filter(item -> !updateProjectDto.getUserIds().contains(item))
                .toList();

        List<String> insertIds = updateProjectDto.getUserIds().stream()
                .filter(item -> !orgUserIds.contains(item))
                .toList();
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))
                    .and(USER_ROLE_RELATION.USER_ID.in(deleteIds)));
            queryChain.list().forEach(userRoleRelation -> {
                User user = userMapper.selectOneById(userRoleRelation.getUserId());
                String logProjectId = OperationLogConstants.SYSTEM;
                if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                    logProjectId = OperationLogConstants.ORGANIZATION;
                }
                LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
            });
            userRoleRelationMapper.deleteByQuery(queryChain);
        }
        if (CollectionUtils.isNotEmpty(insertIds)) {
            ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
            memberRequest.setProjectIds(List.of(project.getId()));
            memberRequest.setUserIds(insertIds);
            this.addProjectAdmin(memberRequest, updateUser, path, OperationLogType.ADD.name(), Translator.get("add"), module);
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            operationLogService.batchAdd(logDTOList);
        }
        mapper.update(project);
        return projectDTO;
    }

    @Override
    public int delete(String id, String deleteUser) {
        // 删除项目删除全部资源 这里的删除只是假删除
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setDeleteUser(deleteUser);
        project.setDeleteTime(LocalDateTime.now());
        return mapper.delete(project);
    }

    @Override
    public void addProjectMember(ProjectAddMemberBatchRequest request, String path, String type, String content, String module) {
        String createUser = SessionUtils.getCurrentUserId();
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            Project project = mapper.selectOneById(projectId);
            Map<String, String> userMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))).exists();
                if (!exists) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setUserId(userId);
                    memberRole.setRoleId(InternalUserRole.PROJECT_MEMBER.getValue());
                    memberRole.setSourceId(projectId);
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(project.getOrganizationId());
                    userRoleRelations.add(memberRole);
                    String logProjectId = OperationLogConstants.SYSTEM;
                    if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                        logProjectId = OperationLogConstants.ORGANIZATION;
                    }
                    LogDTO logDTO = new LogDTO(logProjectId, OperationLogConstants.SYSTEM, memberRole.getId(), createUser, type, module, content + Translator.get("project_member") + ": " + userMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    public int removeProjectMember(String projectId, String userId, String createUser, String module, String path) {
        checkProjectNotExist(projectId);
        List<User> users = QueryChain.of(User.class).where(USER.ID.eq(userId)).list();
        User user = CollectionUtils.isNotEmpty(users) ? users.getFirst() : null;
        if (user == null) {
            throw new MSException(Translator.get("user_not_exist"));
        }
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.ne(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)).
                        and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue())))
                .exists();
        if (!exists) {
            throw new MSException(Translator.get("keep_at_least_one_administrator"));
        }
        if (StringUtils.equals(projectId, user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.update(user);
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        QueryChain<UserRoleRelation> userRoleRelationQueryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)));
        userRoleRelationQueryChain.list().forEach(userRoleRelation -> {
            String logProjectId = OperationLogConstants.SYSTEM;
            if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                logProjectId = OperationLogConstants.ORGANIZATION;
            }
            LogDTO logDTO = new LogDTO(logProjectId, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_member") + ": " + user.getName());
            setLog(logDTO, path, HttpMethodConstants.GET.name(), logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(userRoleRelationQueryChain));
        return 0;
    }

    @Override
    public List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request) {
        List<OptionDTO> options = new ArrayList<>();
        //获取制定组织的资源池  和全部组织的资源池
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getOrganizationId())) {
            List<TestResourcePoolOrganization> orgPools = QueryChain.of(TestResourcePoolOrganization.class)
                    .where(TEST_RESOURCE_POOL_ORGANIZATION.ORG_ID.eq(request.getOrganizationId())).list();
            if (CollectionUtils.isNotEmpty(orgPools)) {
                List<String> poolIds = orgPools.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList();
                testResourcePools.addAll(QueryChain.of(TestResourcePool.class).where(TEST_RESOURCE_POOL.ID.in(poolIds)
                        .and(TEST_RESOURCE_POOL.ENABLE.eq(true))).list());
            }
        }
        testResourcePools.addAll(QueryChain.of(TestResourcePool.class).where(TEST_RESOURCE_POOL.ALL_ORG.eq(true)
                .and(TEST_RESOURCE_POOL.ENABLE.eq(true))).list());
        testResourcePools = testResourcePools.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //这里需要获取项目开启的模块   判断资源池开启的使用范围的模块是否在项目开启的模块中
        List<String> moduleIds = request.getModulesIds();
        testResourcePools.forEach(pool -> {
            if (moduleIds.contains(API_TEST) || moduleIds.contains(TEST_PLAN)) {
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(pool.getId());
                optionDTO.setName(pool.getName());
                options.add(optionDTO);
            }
        });
        return options;
    }

    @Override
    public void rename(UpdateProjectNameRequest request, String userId) {
        checkProjectNotExist(request.getId());
        Project project = new Project();
        project.setId(request.getId());
        project.setName(request.getName());
        project.setOrganizationId(request.getOrganizationId());
        checkProjectExistByName(project);
        project.setUpdateUser(userId);
        mapper.update(project);
    }

    @Override
    public Page<ProjectDTO> getProjectPage(ProjectRequest request) {
        Page<ProjectDTO> page = queryChain()
                .select(PROJECT.ALL_COLUMNS, ORGANIZATION.NAME.as("organization_name"))
                .from(PROJECT).innerJoin(ORGANIZATION).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
                .where(PROJECT.ORGANIZATION_ID.eq(request.getOrganizationId())
                        .and(PROJECT.NAME.like(request.getKeyword()).or(PROJECT.NUM.like(request.getKeyword()))))
                .orderBy(PROJECT.CREATE_TIME.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), ProjectDTO.class);
        buildUserInfo(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void disable(String id) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setEnable(false);
        project.setUpdateUser(SessionUtils.getCurrentUserId());
        mapper.update(project);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void enable(String id) {
        checkProjectNotExist(id);
        Project project = new Project();
        project.setId(id);
        project.setEnable(true);
        project.setUpdateUser(SessionUtils.getCurrentUserId());
        mapper.update(project);
    }

    @Override
    public Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request) {
        QueryWrapper sub = new QueryWrapper()
                .select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.PHONE.like(request.getKeyword()))
                        .or(USER.EMAIL.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        return QueryChain.of(UserRoleRelation.class)
                .select("temp.* ")
                .select(" MAX( if (temp.role_id = 'project_admin', true, false)) as adminFlag")
                .select(" MIN(temp.memberTime) as groupTime")
                .from(new SelectQueryTable(sub).as("temp"))
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime")
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), UserExtendDTO.class);
    }

    @Override
    public int revoke(String id, String currentUserId) {
        // todo
        return 0;
    }

    @Override
    public List<Project> getUserProject(String organizationId, String currentUserId) {
        checkOrg(organizationId);
        User user = userMapper.selectOneById(currentUserId);
        String projectId;
        if (Objects.nonNull(user) && StringUtils.isNoneBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        List<Project> allProject;
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(currentUserId)
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ADMIN.name()))).exists();
        if (exists) {
            allProject = queryChain().where(PROJECT.ENABLE.eq(true)
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId)))
                    .orderBy(PROJECT.NAME.asc())
                    .list();
        } else {
            allProject = queryChain().select(QueryMethods.distinct(PROJECT.ALL_COLUMNS))
                    .from(PROJECT)
                    .join(USER_ROLE_RELATION).on(USER_ROLE_RELATION.SOURCE_ID.eq(PROJECT.ID))
                    .join(USER_ROLE).on(USER_ROLE_RELATION.ROLE_ID.eq(USER_ROLE.ID))
                    .join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                    .where(USER_ROLE_RELATION.USER_ID.eq(currentUserId)
                            .and(USER_ROLE.TYPE.eq("PROJECT"))
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId)
                                    .and(PROJECT.ENABLE.eq(true))))
                    .orderBy(PROJECT.NAME.asc())
                    .list();
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.addFirst(project);
                    return temp;
                })
                .orElse(allProject);
    }

    @Override
    public List<Project> getUserProjectWidthModule(String organizationId, String module, String userId) {
        if (StringUtils.isBlank(module)) {
            throw new MSException(Translator.get("module.name.is.empty"));
        }
        String moduleName = null;
        if (StringUtils.equalsIgnoreCase(module, "API") || StringUtils.equalsIgnoreCase(module, "SCENARIO")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_API_TEST;
        }
        if (StringUtils.equalsIgnoreCase(module, "FUNCTIONAL")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_FUNCTIONAL_CASE;
        }
        if (StringUtils.equalsIgnoreCase(module, "BUG")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_BUG;
        }
        if (StringUtils.equalsIgnoreCase(module, "PERFORMANCE")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_LOAD_TEST;
        }
        if (StringUtils.equalsIgnoreCase(module, "UI")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_UI;
        }
        if (StringUtils.equalsIgnoreCase(module, "TEST_PLAN")) {
            moduleName = ProjectMenuConstants.MODULE_MENU_TEST_PLAN;
        }
        if (StringUtils.isBlank(moduleName)) {
            throw new MSException(Translator.get("module.name.is.error"));
        }
        checkOrg(organizationId);
        User user = userMapper.selectOneById(userId);
        String projectId;
        if (user != null && StringUtils.isNotBlank(user.getLastProjectId())) {
            projectId = user.getLastProjectId();
        } else {
            projectId = null;
        }
        //判断用户是否是系统管理员
        List<Project> allProject;
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ADMIN.name()))).exists();
        if (exists) {
            allProject = queryChain().where(PROJECT.ENABLE.eq(true)
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId)))
                    .and(PROJECT.MODULE_SETTING.like(moduleName))
                    .orderBy(PROJECT.NAME.asc())
                    .list();
        } else {
            allProject = queryChain().select(QueryMethods.distinct(PROJECT.ALL_COLUMNS))
                    .from(PROJECT)
                    .join(USER_ROLE_RELATION).on(USER_ROLE_RELATION.SOURCE_ID.eq(PROJECT.ID))
                    .join(USER_ROLE).on(USER_ROLE_RELATION.ROLE_ID.eq(USER_ROLE.ID))
                    .join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                    .where(USER_ROLE_RELATION.USER_ID.eq(userId)
                            .and(USER_ROLE.TYPE.eq("PROJECT"))
                            .and(PROJECT.ORGANIZATION_ID.eq(organizationId)
                                    .and(PROJECT.ENABLE.eq(true)))
                            .and(PROJECT.MODULE_SETTING.like(moduleName)))
                    .orderBy(PROJECT.NAME.asc())
                    .list();
        }
        List<Project> temp = allProject;
        return allProject.stream()
                .filter(project -> StringUtils.equals(project.getId(), projectId))
                .findFirst()
                .map(project -> {
                    temp.remove(project);
                    temp.addFirst(project);
                    return temp;
                })
                .orElse(allProject);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserDTO switchProject(ProjectSwitchRequest request, String userId) {
        if (!StringUtils.equals(userId, request.getUserId())) {
            throw new MSException(Translator.get("not_authorized"));
        }
        queryChain().where(PROJECT.ID.eq(request.getProjectId())).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("project_not_exist")));
        UpdateChain.of(User.class).set(USER.LAST_PROJECT_ID, request.getProjectId())
                .where(USER.ID.eq(request.getUserId())).update();
        UserDTO userDTO = QueryChain.of(User.class).where(USER.ID.eq(request.getUserId())).oneAs(UserDTO.class);
        UserRolePermissionDTO userRolePermission = baseUserRolePermissionService.getUserRolePermission(request.getUserId());
        userDTO.setUserRoleRelations(userRolePermission.getUserRoleRelations());
        userDTO.setUserRoles(userRolePermission.getUserRoles());
        userDTO.setUserRolePermissions(userRolePermission.getList());
        return userDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ProjectDTO update(ProjectUpdateRequest request, String userId) {
        Project project = new Project();
        ProjectDTO projectDTO = new ProjectDTO();
        project.setId(request.getId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOrganizationId(request.getOrganizationId());
        project.setEnable(request.getEnable());
        project.setUpdateUser(userId);
        checkProjectExistByName(project);
        checkProjectNotExist(project.getId());
        BeanUtils.copyProperties(project, projectDTO);
        mapper.update(project);
        return projectDTO;
    }

    @Override
    public boolean hasPermission(String id, String userId) {
        boolean hasPermission = true;
        boolean exists = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(userId).and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ADMIN.name()))).exists();
        if (exists) {
            return hasPermission;
        }
        if (!queryChain().where(PROJECT.ID.eq(id).and(PROJECT.ENABLE.eq(true))).exists()) {
            return false;
        }
        boolean exists1 = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(userId).and(USER_ROLE_RELATION.SOURCE_ID.eq(id))).exists();
        if (!exists1) {
            return false;
        }
        return hasPermission;
    }

    @Override
    public List<UserExtendDTO> getMemberOption(String projectId, String keyword) {
        Project project = mapper.selectOneById(projectId);
        if (Objects.nonNull(project)) {
            return QueryChain.of(User.class)
                    .select(QueryMethods.distinct(USER.ALL_COLUMNS))
                    .from(USER_ROLE_RELATION)
                    .join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                    .where(USER.ENABLE.eq(true)
                            .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                            .and(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword))))
                    .orderBy(USER.NAME.desc()).limit(1000)
                    .listAs(UserExtendDTO.class);
        }
        return List.of();
    }

    @Override
    public Project checkResourceExist(String projectId) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(projectId), "permission.project.name");
    }

    private void checkOrg(String organizationId) {
        QueryChain.of(Organization.class).where(ORGANIZATION.ID.eq(organizationId))
                .oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("organization_not_exist")));
    }

    private List<ProjectResourcePoolDTO> getProjectResourcePoolDTOList(List<String> projectIds) {
        return QueryChain.of(ProjectTestResourcePool.class)
                .select(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID, TEST_RESOURCE_POOL.ALL_COLUMNS)
                .from(PROJECT_TEST_RESOURCE_POOL)
                .innerJoin(TEST_RESOURCE_POOL).on(TEST_RESOURCE_POOL.ID.eq(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID))
                .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.in(projectIds).and(TEST_RESOURCE_POOL.ENABLE.eq(true)))
                .listAs(ProjectResourcePoolDTO.class);
    }

    private List<ProjectDTO> getProjectExtendDTOList(List<String> projectIds) {
        QueryWrapper sub = new QueryWrapper()
                .select(USER_ROLE_RELATION.SOURCE_ID, USER.ID)
                .from(USER_ROLE_RELATION).leftJoin(USER).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds));
        return queryChain()
                .select(PROJECT.ID).select("count(distinct temp.id) as memberCount")
                .from(PROJECT.as("p")).leftJoin(new SelectQueryTable(sub).as("temp")).on("p.id = temp.source_id")
                .groupBy(PROJECT.ID)
                .listAs(ProjectDTO.class);
    }

    private List<UserExtendDTO> getProjectAdminList(List<String> projectIds) {
        return QueryChain.of(UserRoleRelation.class)
                .select(USER.ALL_COLUMNS, USER_ROLE_RELATION.SOURCE_ID)
                .from(USER_ROLE_RELATION)
                .leftJoin(USER).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds).and(USER_ROLE_RELATION.ROLE_ID.eq("project_admin")))
                .listAs(UserExtendDTO.class);
    }

    private void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            Project project = mapper.selectOneById(projectId);
            Map<String, String> nameMap = addUserPre(request, createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).exists();
                if (!exists) {
                    UserRoleRelation adminRole = new UserRoleRelation();
                    adminRole.setUserId(userId);
                    adminRole.setRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
                    adminRole.setSourceId(projectId);
                    adminRole.setCreateUser(createUser);
                    adminRole.setOrganizationId(project.getOrganizationId());
                    userRoleRelations.add(adminRole);
                    String logProjectId = OperationLogConstants.SYSTEM;
                    if (StringUtils.equals(module, OperationLogModule.SETTING_ORGANIZATION_PROJECT)) {
                        logProjectId = OperationLogConstants.ORGANIZATION;
                    }
                    LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), adminRole.getId(), createUser, type, module, content + Translator.get("project_admin") + ": " + nameMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    private void setLog(LogDTO dto, String path, String method, List<LogDTO> logDTOList) {
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(StringUtils.EMPTY));
        logDTOList.add(dto);
    }

    private Map<String, String> addUserPre(ProjectAddMemberBatchRequest request, String createUser, String path, String module, String projectId, Project project) {
        checkProjectNotExist(projectId);
        List<User> users = QueryChain.of(User.class).where(User::getId).in(request.getUserIds()).list();
        if (request.getUserIds().size() != users.size()) {
            throw new MSException(Translator.get("user_not_exist"));
        }
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        this.checkOrgRoleExit(request.getUserIds(), project.getOrganizationId(), createUser, userMap, path, module);
        return userMap;
    }

    private void checkOrgRoleExit(List<String> userId, String orgId, String createUser, Map<String, String> nameMap, String path, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(orgId))).list();
        //把用户id放到一个新的list
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        if (CollectionUtils.isNotEmpty(userId)) {
            List<UserRoleRelation> userRoleRelation = new ArrayList<>();
            userId.forEach(id -> {
                if (!orgUserIds.contains(id)) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setUserId(id);
                    memberRole.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                    memberRole.setSourceId(orgId);
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(orgId);
                    userRoleRelation.add(memberRole);
                    LogDTO logDTO = new LogDTO(orgId, orgId, memberRole.getId(), createUser, OperationLogType.ADD.name(), module, Translator.get("add") + Translator.get("organization_member") + ": " + nameMap.get(id));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
            if (CollectionUtils.isNotEmpty(userRoleRelation)) {
                userRoleRelationMapper.insertBatch(userRoleRelation);
            }
        }
        operationLogService.batchAdd(logDTOList);
    }

    void checkProjectNotExist(String projectId) {
        if (Objects.isNull(mapper.selectOneById(projectId))) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    private void checkResourcePoolExist(List<String> resourcePoolIds) {
        long count = QueryChain.of(TestResourcePool.class).where(TEST_RESOURCE_POOL.ID.in(resourcePoolIds)
                .and(TEST_RESOURCE_POOL.ENABLE.eq(true))).count();
        if (count != resourcePoolIds.size()) {
            throw new MSException(Translator.get("resource_pool_not_exist"));
        }
    }

    private void checkProjectExistByName(Project project) {
        long count = queryChain().where(PROJECT.NAME.eq(project.getName())
                        .and(PROJECT.ORGANIZATION_ID.eq(project.getOrganizationId())))
                .and(PROJECT.ID.ne(project.getId())).count();
        if (count > 0) {
            throw new MSException(Translator.get("project_name_already_exists"));
        }
    }
}
