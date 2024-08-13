package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.Organization;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationQueryRequest;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.backend.payload.request.system.user.UserRoleBatchRelationRequest;
import cn.master.backend.payload.response.TableBatchProcessResponse;
import cn.master.backend.payload.response.user.UserTableResponse;
import cn.master.backend.service.*;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserRoleTableDef.USER_ROLE;
import static cn.master.backend.entity.table.UserTableDef.USER;
import static cn.master.backend.handler.result.CommonResultCode.USER_ROLE_RELATION_EXIST;
import static cn.master.backend.handler.result.SystemResultCode.GLOBAL_USER_ROLE_LIMIT;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Service("globalUserRoleRelationService")
@RequiredArgsConstructor
public class GlobalUserRoleRelationServiceImpl extends BaseUserRoleRelationServiceImpl implements GlobalUserRoleRelationService {
    private final GlobalUserRoleService globalUserRoleService;
    private final BaseUserRoleService baseUserRoleService;
    private final OperationLogService operationLogService;
    private final UserToolService userToolService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void save(GlobalUserRoleRelationUpdateRequest request) {
        checkGlobalSystemUserRoleLegality(Collections.singletonList(request.getRoleId()));
        if (request.getUserIds().isEmpty()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        val count = QueryChain.of(User.class).where(User::getId).in(request.getUserIds()).count();
        if (count != request.getUserIds().size()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            BeanUtils.copyProperties(request, userRoleRelation);
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
            checkExist(userRoleRelation);
            userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
            userRoleRelations.add(userRoleRelation);
        });
        mapper.insertBatch(userRoleRelations);
    }

    @Override
    public void remove(String id) {
        UserRole userRole = getUserRole(id);
        baseUserRoleService.checkResourceExist(userRole);
        UserRoleRelation userRoleRelation = mapper.selectOneById(id);
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        super.delete(id);
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userRoleRelation.getUserId())
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(UserRoleScope.SYSTEM))).exists();
        if (!exists) {
            throw new MSException(GLOBAL_USER_ROLE_LIMIT);
        }
    }

    @Override
    public Page<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request) {
        UserRole userRole = globalUserRoleService.getById(request.getRoleId());
        globalUserRoleService.checkSystemUserGroup(userRole);
        globalUserRoleService.checkGlobalUserRole(userRole);
        return queryChain()
                .select(USER_ROLE_RELATION.ID)
                .select(USER.ID.as("userId"), USER.EMAIL, USER.NAME, USER.PHONE)
                .from(USER_ROLE_RELATION)
                .innerJoin(USER).on(USER_ROLE_RELATION.USER_ID.eq(USER.ID))
                .where(USER_ROLE_RELATION.ROLE_ID.eq(request.getRoleId()))
                .and(USER.NAME.like(request.getKeyword())
                        .or(USER.EMAIL.like(request.getKeyword()))
                        .or(USER.PHONE.like(request.getKeyword())))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), UserRoleRelationUserDTO.class);
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword) {
        baseUserRoleService.getWithCheck(roleId);
        return super.getExcludeSelectOptionWithLimit(roleId, keyword);
    }

    @Override
    public void updateUserSystemGlobalRole(User user, String operator, List<String> roleList) {
        List<String> deleteRoleList = new ArrayList<>();
        List<UserRoleRelation> saveList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelationList = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(user.getId())
                        .and(USER_ROLE_RELATION.ROLE_ID.in(
                                QueryChain.of(UserRole.class).select(USER_ROLE.ID).from(USER_ROLE)
                                        .where(USER_ROLE.TYPE.eq("SYSTEM").and(USER_ROLE.SCOPE_ID.eq("global"))))
                        ))
                .list();
        List<String> userSavedRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).toList();
        //获取要移除的权限
        for (String userSavedRoleId : userSavedRoleIdList) {
            if (!roleList.contains(userSavedRoleId)) {
                deleteRoleList.add(userSavedRoleId);
            }
        }
        //获取要添加的权限
        for (String roleId : roleList) {
            if (!userSavedRoleIdList.contains(roleId)) {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(user.getId());
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(deleteRoleList)) {
            List<String> deleteIdList = new ArrayList<>();
            userRoleRelationList.forEach(item -> {
                if (deleteRoleList.contains(item.getRoleId())) {
                    deleteIdList.add(item.getId());
                }
            });
            LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteBatchByIds(deleteIdList));
            //记录删除日志
            operationLogService.batchAdd(getBatchLogs(deleteRoleList, user, "updateUser", operator, OperationLogType.DELETE.name()));
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            //系统级权限不会太多，所以暂时不分批处理
            saveList.forEach(item -> mapper.insert(item));
            //记录添加日志
            operationLogService.batchAdd(this.getBatchLogs(saveList.stream().map(UserRoleRelation::getRoleId).toList(),
                    user, "updateUser", operator, OperationLogType.ADD.name()));
        }
    }

    @Override
    public List<UserRoleRelation> selectByUserId(String id) {
        return queryChain().where(USER_ROLE_RELATION.USER_ID.eq(id)).list();
    }

    @Override
    public Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(List<String> userIdList) {
        List<UserRoleRelation> userRoleRelationList = queryChain().where(USER_ROLE_RELATION.USER_ID.in(userIdList)).list();
        List<String> userRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).distinct().toList();
        List<String> sourceIdList = userRoleRelationList.stream().map(UserRoleRelation::getSourceId).distinct().toList();
        Map<String, UserRole> userRoleMap = new HashMap<>();
        Map<String, Organization> organizationMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userRoleIdList)) {
            userRoleMap = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(userRoleIdList)
                            .and(USER_ROLE.SCOPE_ID.eq(UserRoleEnum.GLOBAL.toString()))).list()
                    .stream()
                    .collect(Collectors.toMap(UserRole::getId, item -> item));
        }
        if (CollectionUtils.isNotEmpty(sourceIdList)) {
            organizationMap = QueryChain.of(Organization.class).where(Organization::getId).in(sourceIdList).list().stream()
                    .collect(Collectors.toMap(Organization::getId, item -> item));
        }
        Map<String, UserTableResponse> returnMap = new HashMap<>();
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            UserTableResponse userInfo = returnMap.get(userRoleRelation.getUserId());
            if (userInfo == null) {
                userInfo = new UserTableResponse();
                userInfo.setId(userRoleRelation.getUserId());
                returnMap.put(userRoleRelation.getUserId(), userInfo);
            }
            UserRole userRole = userRoleMap.get(userRoleRelation.getRoleId());
            if (userRole != null && StringUtils.equalsIgnoreCase(userRole.getType(), UserRoleScope.SYSTEM)) {
                userInfo.setUserRole(userRole);
            }
            Organization organization = organizationMap.get(userRoleRelation.getSourceId());
            if (organization != null && !userInfo.getOrganizationList().contains(organization)) {
                userInfo.setOrganization(organization);
            }
        }
        return returnMap;
    }

    @Override
    public void deleteByUserIdList(List<String> userIdList) {
        QueryChain<UserRoleRelation> queryChain = QueryChain.of(UserRoleRelation.class)
                .where(USER_ROLE_RELATION.USER_ID.eq(userIdList));
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public TableBatchProcessResponse batchAdd(UserRoleBatchRelationRequest request, String operator) {
        checkGlobalSystemUserRoleLegality(request.getRoleIds());
        request.setSelectIds(userToolService.getBatchUserIds(request));
        checkUserLegality(request.getSelectIds());
        List<UserRoleRelation> savedUserRoleRelation = queryChain().where(USER_ROLE_RELATION.USER_ID.in(request.getSelectIds())
                .and(USER_ROLE_RELATION.ROLE_ID.in(request.getRoleIds()))).list();
        Map<String, List<String>> userRoleIdMap = savedUserRoleRelation.stream()
                .collect(Collectors.groupingBy(UserRoleRelation::getUserId, Collectors.mapping(UserRoleRelation::getRoleId, Collectors.toList())));
        List<UserRoleRelation> saveList = new ArrayList<>();
        for (String userId : request.getSelectIds()) {
            for (String roleId : request.getRoleIds()) {
                if (userRoleIdMap.containsKey(userId) && userRoleIdMap.get(userId).contains(roleId)) {
                    continue;
                }
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            mapper.insertBatch(saveList);
        }
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(saveList.size());
        return response;
    }

    private void checkUserLegality(@Valid List<String> userIds) {
        if (userIds.isEmpty()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        long count = QueryChain.of(User.class).where(User::getId).in(userIds).count();
        if (count != userIds.size()) {
            throw new MSException(Translator.get("user.id.not.exist"));
        }
    }

    private List<LogDTO> getBatchLogs(@Valid @NotEmpty List<String> userRoleId,
                                      @Valid User user,
                                      @Valid @NotEmpty String operationMethod,
                                      @Valid @NotEmpty String operator,
                                      @Valid @NotEmpty String operationType) {
        List<LogDTO> logs = new ArrayList<>();
        QueryChain<UserRole> queryChain = QueryChain.of(UserRole.class).where(USER_ROLE.ID.in(userRoleId));
        List<UserRole> userRoles = baseUserRoleService.list(queryChain);
        userRoles.forEach(userRole -> {
            LogDTO log = new LogDTO();
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(operationType);
            log.setCreateUser(operator);
            log.setModule(OperationLogModule.SETTING_SYSTEM_USER_SINGLE);
            log.setMethod(operationMethod);
            log.setSourceId(user.getId());
            log.setContent(user.getName() + StringUtils.SPACE
                    + Translator.get(StringUtils.lowerCase(operationType)) + StringUtils.SPACE
                    + Translator.get("permission.project_group.name") + StringUtils.SPACE
                    + userRole.getName());
            log.setOriginalValue(JSON.toJSONBytes(userRole));
            logs.add(log);
        });
        return logs;
    }

    private void checkGlobalSystemUserRoleLegality(List<String> checkIdList) {
        List<UserRole> userRoleList = globalUserRoleService.getList(checkIdList);
        if (userRoleList.size() != checkIdList.size()) {
            throw new MSException(Translator.get("user_role_not_exist"));
        }
        userRoleList.forEach(userRole -> {
            globalUserRoleService.checkSystemUserGroup(userRole);
            globalUserRoleService.checkGlobalUserRole(userRole);
        });
    }

    private void checkExist(UserRoleRelation userRoleRelation) {
        List<UserRoleRelation> userRoleRelations = queryChain()
                .where(USER_ROLE_RELATION.USER_ID.eq(userRoleRelation.getUserId())
                        .and(USER_ROLE_RELATION.ROLE_ID.eq(userRoleRelation.getRoleId()))).list();
        if (!userRoleRelations.isEmpty()) {
            throw new MSException(USER_ROLE_RELATION_EXIST);
        }
    }
}
