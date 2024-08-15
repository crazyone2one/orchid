package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Project;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.system.AddProjectRequest;
import cn.master.backend.payload.request.system.UpdateProjectNameRequest;
import cn.master.backend.payload.request.system.UpdateProjectRequest;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationProjectLogService {
    @Resource
    ProjectMapper projectMapper;

    public LogDTO addLog(AddProjectRequest project) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                project.getOrganizationId(),
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                project.getName());

        dto.setOriginalValue(JSON.toJSONBytes(project));
        return dto;
    }

    /**
     * @param request 接口请求参数
     * @return 日志详情
     */
    public LogDTO updateLog(UpdateProjectRequest request) {
        Project project = projectMapper.selectOneById(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    request.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    public LogDTO renameLog(UpdateProjectNameRequest request) {
        Project project = projectMapper.selectOneById(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    request.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    public LogDTO updateLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    project.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());
            dto.setMethod(HttpMethodConstants.GET.name());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    /**
     * 删除接口日志
     */
    public LogDTO deleteLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }

    /**
     * 恢复项目
     *
     * @param id 接口请求参数
     * @return 日志详情
     */
    public LogDTO recoverLog(String id) {
        Project project = projectMapper.selectOneById(id);
        if (project != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    project.getOrganizationId(),
                    id,
                    null,
                    OperationLogType.RECOVER.name(),
                    OperationLogModule.SETTING_ORGANIZATION_PROJECT,
                    project.getName());
            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }
}
