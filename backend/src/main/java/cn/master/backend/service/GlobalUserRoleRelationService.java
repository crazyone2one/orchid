package cn.master.backend.service;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationQueryRequest;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.payload.dto.user.UserRoleRelationUserDTO;
import cn.master.backend.payload.request.system.user.UserRoleBatchRelationRequest;
import cn.master.backend.payload.response.TableBatchProcessResponse;
import cn.master.backend.payload.response.user.UserTableResponse;
import com.mybatisflex.core.paginate.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface GlobalUserRoleRelationService extends BaseUserRoleRelationService {
    void save(GlobalUserRoleRelationUpdateRequest request);

    void remove(String id);

    Page<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request);

    List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword);

    void updateUserSystemGlobalRole(@Valid User user, @Valid @NotEmpty String operator, @Valid @NotEmpty List<String> roleList);

    List<UserRoleRelation> selectByUserId(String id);

    Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(List<String> userIdList);

    void deleteByUserIdList(List<String> userIdList);

    TableBatchProcessResponse batchAdd(UserRoleBatchRelationRequest request, String operator);
}
