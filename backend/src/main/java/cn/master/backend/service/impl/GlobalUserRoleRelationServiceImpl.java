package cn.master.backend.service.impl;

import cn.master.backend.constants.UserRoleScope;
import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationQueryRequest;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.backend.service.BaseUserRoleService;
import cn.master.backend.service.GlobalUserRoleRelationService;
import cn.master.backend.service.GlobalUserRoleService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
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
