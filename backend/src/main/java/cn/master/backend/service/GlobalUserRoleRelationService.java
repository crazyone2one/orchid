package cn.master.backend.service;

import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationQueryRequest;
import cn.master.backend.payload.dto.system.request.GlobalUserRoleRelationUpdateRequest;
import cn.master.backend.payload.dto.user.UserExcludeOptionDTO;
import cn.master.backend.payload.dto.user.UserRoleRelationUserDTO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public interface GlobalUserRoleRelationService extends BaseUserRoleRelationService {
    void save(GlobalUserRoleRelationUpdateRequest request);

    void remove(String id);

    Page<UserRoleRelationUserDTO> list(GlobalUserRoleRelationQueryRequest request);

    List<UserExcludeOptionDTO> getExcludeSelectOption(String roleId, String keyword);
}
