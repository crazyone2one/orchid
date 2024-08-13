package cn.master.backend.service.impl;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRole;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.UserRoleRelationMapper;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.service.BaseUserRoleRelationService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.master.backend.constants.InternalUserRole.ADMIN;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserTableDef.USER;
import static cn.master.backend.handler.result.CommonResultCode.USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION;

/**
 * 用户组关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Service("baseUserRoleRelationService")
public class BaseUserRoleRelationServiceImpl extends ServiceImpl<UserRoleRelationMapper, UserRoleRelation> implements BaseUserRoleRelationService {

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByRoleId(String roleId) {
        val queryChain = queryChain().where(UserRoleRelation::getRoleId).eq(roleId);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    @Override
    public List<String> getUserIdByRoleId(String roleId) {
        return queryChain().select(USER_ROLE_RELATION.USER_ID)
                .where(USER_ROLE_RELATION.ROLE_ID.eq(roleId))
                .listAs(String.class);
    }

    @Override
    public List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds) {
        return userIds.isEmpty() ? List.of() : queryChain().where(USER_ROLE_RELATION.USER_ID.in(userIds)).list();
    }

    @Override
    public UserRole getUserRole(String id) {
        UserRoleRelation userRoleRelation = mapper.selectOneById(id);
        return Objects.isNull(userRoleRelation) ? null :
                QueryChain.of(UserRole.class).where(UserRole::getId).eq(userRoleRelation.getRoleId()).one();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id) {
        UserRoleRelation userRoleRelation = mapper.selectOneById(id);
        checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId());
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword) {
        List<UserExcludeOptionDTO> selectOptions = QueryChain.of(User.class).where(USER.NAME.like(keyword)
                        .or(USER.EMAIL.like(keyword))).limit(1000)
                .listAs(UserExcludeOptionDTO.class);
        List<String> excludeUserIds = queryChain().select(USER_ROLE_RELATION.USER_ID)
                .from(USER_ROLE_RELATION)
                .where(USER_ROLE_RELATION.ROLE_ID.eq(roleId))
                .listAs(String.class);
        selectOptions.forEach((excludeOption) -> {
            if (excludeUserIds.contains(excludeOption.getId())) {
                excludeOption.setExclude(true);
            }
        });
        return selectOptions;
    }

    private static void checkAdminPermissionRemove(String userId, String roleId) {
        if (StringUtils.equals(roleId, ADMIN.getValue()) && StringUtils.equals(userId, ADMIN.getValue())) {
            throw new MSException(USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }
}
