package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.OrganizationMapper;
import cn.master.backend.mapper.UserMapper;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.system.*;
import cn.master.backend.payload.dto.system.request.OrganizationRequest;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.OrganizationService;
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
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 组织 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-07
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {
    private final UserRoleRelationMapper userRoleRelationMapper;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;
    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    private static final Long DEFAULT_ORGANIZATION_NUM = 100001L;

    @Override
    public Page<OrgUserExtend> getMemberListByOrg(OrganizationRequest request) {
        //根据组织ID获取所有组织用户关系表
        String organizationId = request.getOrganizationId();
        Page<OrgUserExtend> orgUserExtends = mapper.paginateAs(Page.of(request.getCurrent(), request.getPageSize()), listMemberByOrgQuery(request), OrgUserExtend.class);
        Map<String, OrgUserExtend> userMap = orgUserExtends.getRecords()
                .stream().collect(Collectors.toMap(OrgUserExtend::getId, user -> user));
        List<UserRoleRelation> userRoleRelationsByUsers = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.ORGANIZATION_ID.eq(organizationId)
                        .and(USER_ROLE_RELATION.USER_ID.in(new ArrayList<>(userMap.keySet()))))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc())
                .list();
        //根据关系表查询出用户的关联组织和用户组
        Map<String, Set<String>> userIdRoleIdMap = new HashMap<>();
        Map<String, Set<String>> userIdProjectIdMap = new HashMap<>();
        Set<String> roleIdSet = new HashSet<>();
        Set<String> projectIdSet = new HashSet<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelationsByUsers) {
            String sourceId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            //收集组织级别的用户组
            if (StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdRoleIdMap, roleIdSet, roleId, userId);
            }
            //收集项目id
            if (!StringUtils.equals(sourceId, organizationId)) {
                getTargetIds(userIdProjectIdMap, projectIdSet, sourceId, userId);
            }
        }
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(new ArrayList<>(roleIdSet))).list();
        List<Project> projects = new ArrayList<>();
        if (!projectIdSet.isEmpty()) {
            projects = QueryChain.of(Project.class).where(PROJECT.ID.in(projectIdSet)).list();
        }
        for (OrgUserExtend orgUserExtend : orgUserExtends.getRecords()) {
            if (!projects.isEmpty()) {
                Set<String> projectIds = userIdProjectIdMap.get(orgUserExtend.getId());
                if (CollectionUtils.isNotEmpty(projectIds)) {
                    List<Project> projectFilters = projects.stream().filter(t -> projectIds.contains(t.getId())).toList();
                    List<OptionDTO> projectList = new ArrayList<>();
                    setProjectList(projectList, projectFilters);
                    orgUserExtend.setProjectIdNameMap(projectList);
                }
            }

            Set<String> userRoleIds = userIdRoleIdMap.get(orgUserExtend.getId());
            List<UserRole> userRoleFilters = userRoles.stream().filter(t -> userRoleIds.contains(t.getId())).toList();
            List<OptionDTO> userRoleList = new ArrayList<>();
            setUserRoleList(userRoleList, userRoleFilters);
            orgUserExtend.setUserRoleIdNameMap(userRoleList);
        }
        return orgUserExtends;
    }

    private void setUserRoleList(List<OptionDTO> userRoleList, List<UserRole> userRoles) {
        for (UserRole userRole : userRoles) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(userRole.getId());
            optionDTO.setName(userRole.getName());
            userRoleList.add(optionDTO);
        }
    }

    private void setProjectList(List<OptionDTO> projectList, List<Project> projectFilters) {
        for (Project project : projectFilters) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(project.getId());
            optionDTO.setName(project.getName());
            projectList.add(optionDTO);
        }
    }

    private void getTargetIds(Map<String, Set<String>> userIdTargetIdMap, Set<String> targetIdSet, String sourceId, String userId) {
        Set<String> targetIds = userIdTargetIdMap.get(userId);
        if (CollectionUtils.isEmpty(targetIds)) {
            targetIds = new HashSet<>();
        }
        targetIds.add(sourceId);
        targetIdSet.add(sourceId);
        userIdTargetIdMap.put(userId, targetIds);
    }

    private QueryWrapper listMemberByOrgQuery(OrganizationRequest request) {
        QueryWrapper subQuery = QueryChain.create()
                .select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER.UPDATE_TIME.desc());
        return new QueryWrapper().select("temp.*", "min(temp.memberTime) as groupTime")
                .from(new SelectQueryTable(subQuery).as("temp"))
                .groupBy("temp.id").orderBy("groupTime");
    }

    @Override
    public Page<OrganizationDTO> list(OrganizationRequest request) {
        val queryChain = queryChain()
                .where(ORGANIZATION.NAME.like(request.getKeyword())
                        .or(ORGANIZATION.NUM.like(request.getKeyword())))
                .orderBy(ORGANIZATION.CREATE_TIME.desc());
        val page = mapper.paginateWithRelationsAs(Page.of(request.getCurrent(), request.getPageSize()), queryChain, OrganizationDTO.class);
        if (page.getRecords().isEmpty()) {
            return new Page<>();
        }
        buildExtraInfo(page.getRecords(), "");
        return page;
    }

    private void buildExtraInfo(List<OrganizationDTO> organizations, String currentUser) {
        List<String> ids = organizations.stream().map(OrganizationDTO::getId).toList();
        QueryWrapper membersGroup = new QueryWrapper()
                .select(USER_ROLE_RELATION.SOURCE_ID, QueryMethods.count(QueryMethods.distinct(USER.ID)).as("membercount"))
                .from(USER_ROLE_RELATION)
                .join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(ids)).groupBy(USER_ROLE_RELATION.SOURCE_ID);
        QueryWrapper projectsGroup = new QueryWrapper()
                .select(PROJECT.ORGANIZATION_ID, QueryMethods.count(PROJECT.ID).as("projectcount"))
                .from(PROJECT).where(PROJECT.ORGANIZATION_ID.in(ids)).groupBy(PROJECT.ORGANIZATION_ID);
        List<OrganizationCountDTO> orgCountList = queryChain()
                .select(ORGANIZATION.ID)
                .select("coalesce(membercount, 0) as memberCount", "coalesce(projectcount, 0) as projectCount")
                .from(ORGANIZATION.as("o"))
                .leftJoin(new SelectQueryTable(membersGroup).as("members_group")).on("o.id = members_group.source_id")
                .leftJoin(new SelectQueryTable(projectsGroup).as("projects_group")).on("o.id = projects_group.organization_id")
                .listAs(OrganizationCountDTO.class);
        Map<String, OrganizationCountDTO> orgCountMap = orgCountList.stream().collect(Collectors.toMap(OrganizationCountDTO::getId, count -> count));
        organizations.forEach(organization -> {
            organization.setProjectCount(orgCountMap.get(organization.getId()).getProjectCount());
            organization.setMemberCount(orgCountMap.get(organization.getId()).getMemberCount());
        });
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void update(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        mapper.update(organizationDTO);
        // 新增的组织管理员ID
        List<String> addOrgAdmins = organizationDTO.getUserIds();
        // 旧的组织管理员ID
        List<String> oldOrgAdmins = getOrgAdminIds(organizationDTO.getId());
        // 需要新增组织管理员ID
        List<String> addIds = addOrgAdmins.stream().filter(addOrgAdmin -> !oldOrgAdmins.contains(addOrgAdmin)).toList();
        // 需要删除的组织管理员ID
        List<String> deleteIds = oldOrgAdmins.stream().filter(oldOrgAdmin -> !addOrgAdmins.contains(oldOrgAdmin)).toList();
        // 添加组织管理员
        if (CollectionUtils.isNotEmpty(addIds)) {
            addIds.forEach(userId -> {
                // 添加组织管理员
                createAdmin(userId, organizationDTO.getId(), organizationDTO.getUpdateUser());
            });
        }
        // 删除组织管理员
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationDTO.getId())
                    .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))
                    .and(USER_ROLE_RELATION.USER_ID.in(deleteIds)));
            LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        }
    }

    private void createAdmin(String memberId, String organizationId, String createUser) {
        UserRoleRelation orgAdmin = new UserRoleRelation();
        orgAdmin.setUserId(memberId);
        orgAdmin.setRoleId(InternalUserRole.ORG_ADMIN.getValue());
        orgAdmin.setSourceId(organizationId);
        orgAdmin.setCreateUser(createUser);
        orgAdmin.setOrganizationId(organizationId);
        userRoleRelationMapper.insertSelective(orgAdmin);
    }

    @Override
    public List<String> getOrgAdminIds(String organizationId) {
        return QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))).list()
                .stream()
                .map(UserRoleRelation::getUserId).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateName(OrganizationDTO organizationDTO) {
        checkOrganizationNotExist(organizationDTO.getId());
        checkOrganizationExist(organizationDTO);
        mapper.update(organizationDTO);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id) {
        checkOrgDefault(id);
        checkOrganizationNotExist(id);
        Organization organization = mapper.selectOneById(id);
        organization.setDeleteTime(LocalDateTime.now());
        organization.setDeleteUser(SessionUtils.getCurrentUserId());
        mapper.delete(organization);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void recover(String id) {
        checkOrganizationNotExist(id);
        updateChain().set(ORGANIZATION.DELETED, false).where(ORGANIZATION.ID.eq(id)).update();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void enable(String id) {
        checkOrganizationNotExist(id);
        updateChain().set(ORGANIZATION.ENABLE, true).where(ORGANIZATION.ID.eq(id)).update();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void disable(String id) {
        checkOrganizationNotExist(id);
        updateChain().set(ORGANIZATION.ENABLE, false).where(ORGANIZATION.ID.eq(id)).update();
    }

    @Override
    public List<OptionDTO> listOptionAll() {
        List<Organization> organizations = queryChain().list();
        return organizations.stream().map(o -> new OptionDTO(o.getId(), o.getName())).toList();
    }

    @Override
    public Page<UserExtendDTO> getMemberListBySystem(OrganizationRequest request) {
        QueryWrapper sub = new QueryWrapper()
                .select(USER.ALL_COLUMNS, USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).join(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId())
                        .and(USER.NAME.like(request.getKeyword())
                                .or(USER.EMAIL.like(request.getKeyword()))
                                .or(USER.PHONE.like(request.getKeyword()))))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        QueryWrapper wrapper = new QueryWrapper().select("temp.*")
                .select("max(if(temp.role_id = 'org_admin', true, false)) as adminFlag")
                .select("min(temp.memberTime) as groupTime")
                .from(new SelectQueryTable(sub).as("temp"))
                .groupBy("temp.id")
                .orderBy("adminFlag", "groupTime");
        return mapper.paginateAs(Page.of(request.getCurrent(), request.getPageSize()), wrapper, UserExtendDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addMemberBySystem(OrganizationMemberRequest request, String currentUserId) {
        OrganizationMemberBatchRequest batchRequest = new OrganizationMemberBatchRequest();
        batchRequest.setOrganizationIds(List.of(request.getOrganizationId()));
        batchRequest.setUserIds(request.getUserIds());
        addMemberBySystem(batchRequest, currentUserId);
        List<String> nameList = QueryChain.of(User.class).where(USER.ID.in(request.getUserIds())).list()
                .stream().map(User::getName).toList();
        List<LogDTO> logs = new ArrayList<>();
        setLog(request.getOrganizationId(), currentUserId, OperationLogType.ADD.name(), Translator.get("add") + Translator.get("organization_member_log") + ": " + StringUtils.join(nameList, ","), ADD_MEMBER_PATH, null, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addMemberBySystem(OrganizationMemberBatchRequest batchRequest, String createUserId) {
        checkOrgExistByIds(batchRequest.getOrganizationIds());
        Map<String, User> userMap = checkUserExist(batchRequest.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        batchRequest.getOrganizationIds().forEach(organizationId -> {
            for (String userId : batchRequest.getUserIds()) {
                if (userMap.get(userId) == null) {
                    throw new MSException(Translator.get("user.not.exist") + ", id: " + userId);
                }
                //组织用户关系已存在, 不再重复添加
                long count = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                        .and(USER_ROLE_RELATION.USER_ID.eq(userId))).count();
                if (count > 0) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setSourceId(organizationId);
                userRoleRelation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelation.setOrganizationId(organizationId);
                userRoleRelations.add(userRoleRelation);
            }
        });
        if (!userRoleRelations.isEmpty()) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void removeMember(String organizationId, String userId, String currentUserId) {
        List<LogDTO> logs = new ArrayList<>();
        checkOrgExistById(organizationId);
        long count = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                .and(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ORG_ADMIN.getValue()))
                .and(USER_ROLE_RELATION.USER_ID.ne(userId))).count();
        if (count == 0) {
            throw new MSException(Translator.get("keep_at_least_one_administrator"));
        }
        List<String> projectIds = getProjectIds(organizationId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds)
                    .and(USER_ROLE_RELATION.USER_ID.eq(userId)));
            LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        }
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                .and(USER_ROLE_RELATION.USER_ID.eq(userId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        User user = QueryChain.of(User.class).where(USER.ID.eq(userId)).one();
        setLog(organizationId, currentUserId, OperationLogType.DELETE.name(), Translator.get("delete") + Translator.get("organization_member_log") + ": " + user.getName(), REMOVE_MEMBER_PATH, user, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public Map<String, Long> getTotal(String organizationId) {
        Map<String, Long> total = new HashMap<>();
        if (StringUtils.isBlank(organizationId)) {
            // 统计所有项目
            total.put("projectTotal", QueryChain.of(Project.class).count());
            total.put("organizationTotal", mapper.selectCountByQuery(new QueryWrapper()));
        } else {
            // 统计组织下的项目
            //projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
            total.put("projectTotal", QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(organizationId)).count());
            total.put("organizationTotal", 1L);
        }
        return total;
    }

    @Override
    public Organization checkResourceExist(String scopeId) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(scopeId), "permission.system_organization_project.name");
    }

    @Override
    public List<UserExtendDTO> getMemberOption(String sourceId, String keyword) {
        return QueryChain.of(User.class).select(QueryMethods.distinct(USER.ALL_COLUMNS))
                .select("count(urr.id) > 0 as memberFlag")
                .from(USER)
                .leftJoin(USER_ROLE_RELATION.as("urr")).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(sourceId)))
                .where(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword)))
                .groupBy(USER.ID).limit(1000).listAs(UserExtendDTO.class);

    }

    @Override
    public void addMemberByOrg(OrganizationMemberExtendRequest organizationMemberExtendRequest, String currentUserId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap;
        userMap = getUserMap(organizationMemberExtendRequest);
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds(), organizationId);
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, currentUserId, userMap, userRoleMap, true);
    }

    @Override
    public void addMemberRole(OrganizationMemberExtendRequest organizationMemberExtendRequest, String userId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExistById(organizationId);
        Map<String, User> userMap;
        userMap = getUserMap(organizationMemberExtendRequest);
        Map<String, UserRole> userRoleMap = checkUseRoleExist(organizationMemberExtendRequest.getUserRoleIds(), organizationId);
        //在新增组织成员与用户组和组织的关系
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, userId, userMap, userRoleMap, false);
    }

    @Override
    public void updateMember(OrganizationMemberUpdateRequest organizationMemberUpdateRequest, String createUserId) {
        String organizationId = organizationMemberUpdateRequest.getOrganizationId();
        //校验组织是否存在
        checkOrgExistById(organizationId);
        //校验用户是否存在
        String memberId = organizationMemberUpdateRequest.getMemberId();
        User user = QueryChain.of(User.class).where(USER.ID.eq(memberId)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("user.not.exist")));
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(memberId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId))).list();
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            throw new MSException(Translator.get("organization_member_not_exist"));
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        //更新用户组
        List<String> userRoleIds = organizationMemberUpdateRequest.getUserRoleIds();
        updateUserRoleRelation(createUserId, organizationId, user, userRoleIds, logDTOList);
        //更新项目
        List<String> projectIds = organizationMemberUpdateRequest.getProjectIds();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            updateProjectUserRelation(createUserId, organizationId, user, projectIds, logDTOList);
        } else {
            List<Project> projects = QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(organizationId)).list();
            if (CollectionUtils.isNotEmpty(projects)) {
                List<String> projectInDBInOrgIds = projects.stream().map(Project::getId).toList();
                QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(memberId)
                        .and(USER_ROLE_RELATION.SOURCE_ID.in(projectInDBInOrgIds)));
                LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
                //add Log
                for (String projectInDBInOrgId : projectInDBInOrgIds) {
                    String path = "/organization/update-member";
                    LogDTO dto = new LogDTO(
                            projectInDBInOrgId,
                            organizationId,
                            memberId,
                            createUserId,
                            OperationLogType.UPDATE.name(),
                            OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                            user.getName());
                    setLog(dto, path, logDTOList, "");
                }
            }
        }
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    public void addMemberToProject(OrgMemberExtendProjectRequest orgMemberExtendProjectRequest, String userId) {
        String requestOrganizationId = orgMemberExtendProjectRequest.getOrganizationId();
        checkOrgExistById(requestOrganizationId);
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> projectIds = orgMemberExtendProjectRequest.getProjectIds();
        //用户不在当前组织内过掉
        Map<String, User> userMap;
        if (orgMemberExtendProjectRequest.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyProperties(orgMemberExtendProjectRequest, organizationRequest);
            List<OrgUserExtend> orgUserExtends = mapper.selectListByQueryAs(listMemberByOrgQuery(organizationRequest), OrgUserExtend.class);
            List<String> excludeIds = orgMemberExtendProjectRequest.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = orgUserExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = orgUserExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }
        } else {
            userMap = checkUserExist(orgMemberExtendProjectRequest.getMemberIds());
        }
        List<String> userIds = userMap.values().stream().map(User::getId).toList();
        userIds.forEach(memberId -> {
            projectIds.forEach(projectId -> {
                //过滤已存在的关系
                List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class)
                        .where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)
                                .and(USER_ROLE_RELATION.USER_ID.eq(memberId)))
                        .list();
                if (CollectionUtils.isEmpty(userRoleRelations)) {
                    UserRoleRelation userRoleRelation = buildUserRoleRelation(userId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue(), requestOrganizationId);
                    userRoleRelation.setOrganizationId(orgMemberExtendProjectRequest.getOrganizationId());
                    userRoleRelationMapper.insert(userRoleRelation);
                    //add Log
                    LogDTO dto = new LogDTO(
                            projectId,
                            requestOrganizationId,
                            memberId,
                            userId,
                            OperationLogType.ADD.name(),
                            OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                            "");
                    setLog(dto, "/organization/project/add-member", logDTOList, userRoleRelation);
                }
            });
        });
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    public List<LogDTO> batchDelLog(String organizationId, String userId) {
        List<String> projectIds = getProjectIds(organizationId);
        List<LogDTO> dtoList = new ArrayList<>();
        User user = userMapper.selectOneById(userId);
        if (CollectionUtils.isNotEmpty(projectIds)) {
            List<UserRoleRelation> userRoleWidthProjectRelations = QueryChain.of(UserRoleRelation.class)
                    .where(USER_ROLE_RELATION.USER_ID.eq(user).and(USER_ROLE_RELATION.SOURCE_ID.in(projectIds))).list();
            //记录项目日志
            for (UserRoleRelation userRoleWidthProjectRelation : userRoleWidthProjectRelations) {
                LogDTO dto = new LogDTO(
                        userRoleWidthProjectRelation.getSourceId(),
                        organizationId,
                        userId,
                        userRoleWidthProjectRelation.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                        user.getName());

                dto.setPath("/organization/remove-member/{organizationId}/{userId}");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthProjectRelation));
                dtoList.add(dto);
            }
        }
        List<UserRoleRelation> userRoleWidthOrgRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId))).list();
        //记录组织日志
        for (UserRoleRelation userRoleWidthOrgRelation : userRoleWidthOrgRelations) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    userRoleWidthOrgRelation.getId(),
                    userRoleWidthOrgRelation.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());

            dto.setPath("/organization/remove-member/{organizationId}/{userId}");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(userRoleWidthOrgRelation));
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public List<OptionDTO> getUserRoleList(String organizationId) {
        checkOrgExistById(organizationId);
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        List<OptionDTO> userRoleList = new ArrayList<>();
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.toString())
                .and(USER_ROLE.SCOPE_ID.in(scopeIds))).list();
        setUserRoleList(userRoleList, userRoles);
        return userRoleList;
    }

    @Override
    public List<OptionDisabledDTO> getUserList(String organizationId, String keyword) {
        checkOrgExistById(organizationId);
        List<OptionDisabledDTO> optionDisabledDTOS = QueryChain.of(User.class)
                .select(USER.ID,USER.EMAIL,USER.NAME).from(USER)
                .where(USER.NAME.like(keyword).or(USER.EMAIL.like(keyword)).or(USER.PHONE.like(keyword)))
                .orderBy(USER.UPDATE_TIME.desc()).limit(1000)
                .listAs(OptionDisabledDTO.class);
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)).list();
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        for (OptionDisabledDTO optionDisabledDTO : optionDisabledDTOS) {
            if (CollectionUtils.isNotEmpty(userIds) && userIds.contains(optionDisabledDTO.getId())) {
                optionDisabledDTO.setDisabled(true);
            }
        }
        return optionDisabledDTOS;
    }

    @Override
    public List<OptionDTO> getProjectList(String organizationId, String keyword) {
        checkOrgExistById(organizationId);
        return QueryChain.of(Project.class)
                .select(PROJECT.ID, PROJECT.NAME).from(PROJECT)
                .where(PROJECT.ORGANIZATION_ID.eq(organizationId).and(PROJECT.NAME.like(keyword)))
                .listAs(OptionDTO.class);
    }

    private void updateProjectUserRelation(String createUserId, String organizationId, User user, List<String> projectIds, List<LogDTO> logDTOList) {
        Map<String, Project> projectMap = checkProjectExist(projectIds, organizationId);
        List<String> projectInDBInOrgIds = projectMap.values().stream().map(Project::getId).toList();
        //删除旧的关系
        String memberId = user.getId();
        List<Project> projects = QueryChain.of(Project.class).where(PROJECT.ORGANIZATION_ID.eq(organizationId)).list();
        List<String> projectIdsAll = projects.stream().map(Project::getId).toList();
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(memberId)
                .and(USER_ROLE_RELATION.SOURCE_ID.in(projectIdsAll)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        projectInDBInOrgIds.forEach(projectId -> {
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, projectId, InternalUserRole.PROJECT_MEMBER.getValue(), organizationId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
            //add Log
            String path = "/organization/update-member";
            LogDTO dto = new LogDTO(
                    projectId,
                    organizationId,
                    memberId,
                    createUserId,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                    user.getName());
            setLog(dto, path, logDTOList, userRoleRelation);
        });
    }

    private Map<String, Project> checkProjectExist(List<String> projectIds, String organizationId) {
        List<Project> projects = QueryChain.of(Project.class).where(PROJECT.ID.in(projectIds)
                .and(PROJECT.ORGANIZATION_ID.eq(organizationId))).list();
        if (CollectionUtils.isEmpty(projects)) {
            throw new MSException(Translator.get("project_not_exist"));
        }
        return projects.stream().collect(Collectors.toMap(Project::getId, project -> project));
    }

    private void updateUserRoleRelation(String createUserId, String organizationId, User user, List<String> userRoleIds, List<LogDTO> logDTOList) {
        //检查用户组是否是组织级别用户组
        String memberId = user.getId();
        Map<String, UserRole> userRoleMap = checkUseRoleExist(userRoleIds, organizationId);
        List<String> userRoleInDBInOrgIds = userRoleMap.values().stream().map(UserRole::getId).toList();
        //删除旧的关系
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(memberId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> userRoleRelationMapper.deleteByQuery(queryChain));
        userRoleInDBInOrgIds.forEach(userRoleId -> {
            UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
            userRoleRelation.setOrganizationId(organizationId);
            userRoleRelationMapper.insert(userRoleRelation);
            //add Log
            String path = "/organization/update-member";
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    organizationId,
                    memberId,
                    createUserId,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                    user.getName());
            setLog(dto, path, logDTOList, userRoleRelation);
        });
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId, Map<String, User> userMap, Map<String, UserRole> userRoleMap, boolean add) {
        List<LogDTO> logDTOList = new ArrayList<>();
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        userMap.keySet().forEach(memberId -> {
            if (userMap.get(memberId) == null) {
                throw new MSException("id:" + memberId + Translator.get("user.not.exist"));
            }
            organizationMemberExtendRequest.getUserRoleIds().forEach(userRoleId -> {
                if (Objects.nonNull(userRoleMap.get(userRoleId))) {
                    List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(organizationId)
                            .and(USER_ROLE_RELATION.USER_ID.eq(memberId))
                            .and(USER_ROLE_RELATION.ROLE_ID.eq(userRoleId))).list();
                    if (CollectionUtils.isEmpty(userRoleRelations)) {
                        UserRoleRelation userRoleRelation = buildUserRoleRelation(createUserId, memberId, organizationId, userRoleId, organizationId);
                        userRoleRelation.setOrganizationId(organizationId);
                        userRoleRelationMapper.insert(userRoleRelation);
                        //add Log
                        String path = add ? "/organization/add-member" : "/organization/role/update-member";
                        String type = add ? OperationLogType.ADD.name() : OperationLogType.UPDATE.name();
                        LogDTO dto = new LogDTO(
                                OperationLogConstants.ORGANIZATION,
                                organizationId,
                                memberId,
                                createUserId,
                                type,
                                OperationLogModule.SETTING_ORGANIZATION_MEMBER,
                                userMap.get(memberId).getName());
                        setLog(dto, path, logDTOList, userRoleRelation);
                    }
                }
            });
        });
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private UserRoleRelation buildUserRoleRelation(String createUserId, String memberId, String sourceId, String roleId, String organizationId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId(memberId);
        userRoleRelation.setOrganizationId(organizationId);
        userRoleRelation.setSourceId(sourceId);
        userRoleRelation.setRoleId(roleId);
        userRoleRelation.setCreateUser(createUserId);
        return userRoleRelation;
    }

    private static void setLog(LogDTO dto, String path, List<LogDTO> logDTOList, Object originalValue) {
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        logDTOList.add(dto);
    }

    private Map<String, UserRole> checkUseRoleExist(List<String> userRoleIds, String organizationId) {
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(userRoleIds)
                .and(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.toString()))
                .and(USER_ROLE.SCOPE_ID.in(Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId)))).list();
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        return userRoles.stream().collect(Collectors.toMap(UserRole::getId, user -> user));
    }

    private Map<String, User> getUserMap(OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        Map<String, User> userMap;
        if (organizationMemberExtendRequest.isSelectAll()) {
            OrganizationRequest organizationRequest = new OrganizationRequest();
            BeanUtils.copyProperties(organizationMemberExtendRequest, organizationRequest);
            List<UserExtendDTO> userExtends = mapper.selectListByQueryAs(listMemberByOrgQuery(organizationRequest), UserExtendDTO.class);
            List<String> excludeIds = organizationMemberExtendRequest.getExcludeIds();
            if (CollectionUtils.isNotEmpty(excludeIds)) {
                userMap = userExtends.stream().filter(user -> !excludeIds.contains(user.getId())).collect(Collectors.toMap(User::getId, user -> user));
            } else {
                userMap = userExtends.stream().collect(Collectors.toMap(User::getId, user -> user));
            }
        } else {
            userMap = checkUserExist(organizationMemberExtendRequest.getMemberIds());
        }
        return userMap;
    }

    private void setLog(String organizationId, String createUser, String type, String content, String path, Object originalValue, Object modifiedValue, List<LogDTO> logs) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                organizationId,
                createUser,
                type,
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                content);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        dto.setModifiedValue(JSON.toJSONBytes(modifiedValue));
        logs.add(dto);
    }

    private List<String> getProjectIds(String organizationId) {
        return List.of();
    }

    private void checkOrgExistById(String organizationId) {
        queryChain().where(ORGANIZATION.ID.eq(organizationId)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("organization_not_exist")));
    }

    private Map<String, User> checkUserExist(@NotEmpty(message = "{user.id.not_blank}") List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MSException(Translator.get("user.not.empty"));
        }
        List<User> users = QueryChain.of(User.class).where(USER.ID.in(userIds)).list();
        if (CollectionUtils.isEmpty(users)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    private void checkOrgExistByIds(List<String> organizationIds) {
        List<Organization> organizations = mapper.selectListByIds(organizationIds);
        if (organizations.size() < organizationIds.size()) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    private void checkOrgDefault(String id) {
        Organization organization = mapper.selectOneById(id);
        if (organization.getNum().equals(DEFAULT_ORGANIZATION_NUM)) {
            throw new MSException(Translator.get("default_organization_not_allow_delete"));
        }
    }

    private void checkOrganizationExist(OrganizationDTO organizationDTO) {
        boolean exists = queryChain().where(ORGANIZATION.NAME.likeRaw(organizationDTO.getName())
                .and(ORGANIZATION.ID.ne(organizationDTO.getId()))).exists();
        if (exists) {
            throw new MSException(Translator.get("organization_name_already_exists"));
        }
    }

    private void checkOrganizationNotExist(String id) {
        queryChain().where(ORGANIZATION.ID.eq(id)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("organization_not_exist")));
    }
}
