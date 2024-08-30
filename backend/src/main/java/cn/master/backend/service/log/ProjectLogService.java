package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Project;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.project.ProjectUpdateRequest;
import cn.master.backend.util.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 08/30/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectLogService {
    @Resource
    private ProjectMapper projectMapper;

    public LogDTO updateLog(ProjectUpdateRequest request) {
        Project project = projectMapper.selectOneById(request.getId());
        if (project != null) {
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    project.getId(),
                    project.getCreateUser(),
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
                    request.getName());

            dto.setOriginalValue(JSON.toJSONBytes(project));
            return dto;
        }
        return null;
    }
}
