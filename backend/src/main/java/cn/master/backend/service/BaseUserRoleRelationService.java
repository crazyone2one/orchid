package cn.master.backend.service;

import cn.master.backend.entity.UserRole;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserRoleRelation;

import java.util.List;

/**
 * 用户组关系 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface BaseUserRoleRelationService extends IService<UserRoleRelation> {

    void deleteByRoleId(String roleId);

    List<String> getUserIdByRoleId(String roleId);

    List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds);

    UserRole getUserRole(String id);

    void delete(String id);

    List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword);
}
