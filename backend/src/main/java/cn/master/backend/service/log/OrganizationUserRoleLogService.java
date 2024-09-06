package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.UserRole;
import cn.master.backend.mapper.UserRoleMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.OrganizationUserRoleEditRequest;
import cn.master.backend.payload.request.system.OrganizationUserRoleMemberEditRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationUserRoleLogService {
    @Resource
    UserRoleMapper userRoleMapper;

    /**
     * 新增组织-用户组
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO addLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 更新组织-用户组
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(OrganizationUserRoleEditRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                request.getName());
        UserRole userRole = userRoleMapper.selectOneById(request.getId());
        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        dto.setModifiedValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 删除组织-用户组
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = userRoleMapper.selectOneById(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        return dto;
    }

    /**
     * 更新组织-用户组-权限
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updatePermissionSettingLog(PermissionSettingUpdateRequest request) {
        LogDTO dto = getLog(request.getUserRoleId());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 更新组织-用户组-成员
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO editMemberLog(OrganizationUserRoleMemberEditRequest request) {
        UserRole userRole = userRoleMapper.selectOneById(request.getUserRoleId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                request.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        return dto;
    }

    private LogDTO getLog(String roleId) {
        UserRole userRole = userRoleMapper.selectOneById(roleId);
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                userRole.getScopeId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.SETTING_ORGANIZATION_USER_ROLE,
                userRole.getName());
    }
}
