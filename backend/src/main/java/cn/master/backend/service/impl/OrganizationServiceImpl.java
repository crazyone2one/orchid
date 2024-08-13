package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.OrganizationMapper;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.OrganizationDTO;
import cn.master.backend.payload.dto.system.request.OrganizationRequest;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationMemberBatchRequest;
import cn.master.backend.payload.request.system.OrganizationMemberRequest;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.service.OrganizationService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SelectQueryTable;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.OrganizationTableDef.ORGANIZATION;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
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
    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    private static final Long DEFAULT_ORGANIZATION_NUM = 100001L;

    @Override
    public Page<Organization> getMemberListByOrg(OrganizationRequest request) {
        return new Page<>();
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
        return page;
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
