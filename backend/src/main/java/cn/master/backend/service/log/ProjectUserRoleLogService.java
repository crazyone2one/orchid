package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.UserRole;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.mapper.UserRoleMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.project.ProjectUserRoleEditRequest;
import cn.master.backend.payload.request.project.ProjectUserRoleMemberEditRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.util.JSON;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectUserRoleLogService {
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ProjectMapper projectMapper;

    /**
     * 新增项目-用户组
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO addLog(ProjectUserRoleEditRequest request) {
        Project project = getProject(request.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 更新项目-用户组
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(ProjectUserRoleEditRequest request) {
        Project project = getProject(request.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                request.getName());

        UserRole userRole = QueryChain.of(UserRole.class).where(UserRole::getId).eq(request.getId()).one();
        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        dto.setModifiedValue(JSON.toJSONBytes(request.getName()));
        return dto;
    }

    /**
     * 删除项目-用户组
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = userRoleMapper.selectOneById(id);
        Project project = getProject(userRole.getScopeId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole.getName()));
        return dto;
    }

    /**
     * 更新项目-用户组-权限
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
     * 更新项目-用户组-成员
     *
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO editMemberLog(ProjectUserRoleMemberEditRequest request) {
        Project project = getProject(request.getProjectId());
        UserRole userRole = userRoleMapper.selectOneById(request.getUserRoleId());
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());
        dto.setType(OperationLogType.UPDATE.name());
        dto.setModifiedValue(JSON.toJSONBytes(request));
        return dto;
    }

    private LogDTO getLog(String roleId) {
        UserRole userRole = userRoleMapper.selectOneById(roleId);
        Project project = getProject(userRole.getScopeId());
        return new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_USER_ROLE,
                userRole.getName());
    }

    private Project getProject(String id) {
        return projectMapper.selectOneById(id);
    }
}
