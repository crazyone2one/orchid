package cn.master.backend.service;

import cn.master.backend.entity.Organization;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.OrganizationDTO;
import cn.master.backend.payload.dto.system.request.OrganizationRequest;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.OrganizationMemberBatchRequest;
import cn.master.backend.payload.request.system.OrganizationMemberRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 组织 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-07
 */
public interface OrganizationService extends IService<Organization> {

    Page<Organization> getMemberListByOrg(OrganizationRequest request);

    Page<OrganizationDTO> list(OrganizationRequest request);

    void update(OrganizationDTO organizationDTO);

    List<String> getOrgAdminIds(String organizationId);

    void updateName(OrganizationDTO organizationDTO);

    void delete(String id);

    void recover(String id);

    void enable(String id);

    void disable(String id);

    List<OptionDTO> listOptionAll();

    Page<UserExtendDTO> getMemberListBySystem(OrganizationRequest request);

    void addMemberBySystem(OrganizationMemberRequest request, String currentUserId);

    void addMemberBySystem(OrganizationMemberBatchRequest batchRequest, String createUserId);

    void removeMember(String organizationId, String userId, String currentUserId);

    Map<String, Long> getTotal(String organizationId);
}
