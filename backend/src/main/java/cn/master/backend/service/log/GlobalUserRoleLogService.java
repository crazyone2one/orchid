package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.UserRole;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.request.UserRoleUpdateRequest;
import cn.master.backend.payload.request.system.PermissionSettingUpdateRequest;
import cn.master.backend.service.BaseUserRoleService;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/02/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalUserRoleLogService {
    @Resource
    private BaseUserRoleService baseUserRoleService;

    /**
     * 添加接口日志
     *
     */
    public LogDTO addLog(UserRoleUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                request.getName());

        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * @param request
     * @return
     */
    public LogDTO updateLog(UserRoleUpdateRequest request) {
        UserRole userRole = baseUserRoleService.get(request.getId());
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    userRole.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JSON.toJSONBytes(userRole));
        }
        return dto;
    }

    public LogDTO updateLog(PermissionSettingUpdateRequest request) {
        UserRole userRole = baseUserRoleService.get(request.getUserRoleId());
        LogDTO dto = null;
        if (userRole != null) {
            dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getUserRoleId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                    userRole.getName());

            dto.setOriginalValue(JSON.toJSONBytes(request));
        }
        return dto;
    }


    /**
     * 删除接口日志
     *
     * @param id
     * @return
     */
    public LogDTO deleteLog(String id) {
        UserRole userRole = baseUserRoleService.get(id);
        if (userRole == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                userRole.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.SETTING_SYSTEM_USER_GROUP,
                userRole.getName());

        dto.setOriginalValue(JSON.toJSONBytes(userRole));
        return dto;
    }
}
