package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.payload.dto.LogDTOBuilder;
import cn.master.backend.payload.dto.project.NodeSortDTO;
import cn.master.backend.payload.dto.system.BaseModule;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanModuleLogService {
    private final ProjectMapper projectMapper;
    private final OperationLogService operationLogService;
    private String logModule = OperationLogModule.TEST_PLAN_MODULE;

    public void saveAddLog(TestPlanModule module, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
    public void saveUpdateLog(TestPlanModule oldModule, TestPlanModule newModule, String projectId, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(newModule.getId())
                .content(newModule.getName())
                .originalValue(JSON.toJSONBytes(oldModule))
                .modifiedValue(JSON.toJSONBytes(newModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
    public void saveDeleteLog(TestPlanModule deleteModule, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(deleteModule.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteModule.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteModule.getId())
                .content(deleteModule.getName() + " " + Translator.get("log.delete_module"))
                .originalValue(JSON.toJSONBytes(deleteModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveMoveLog(@Validated NodeSortDTO request, String operator, String requestUrl, String requestMethod) {
        BaseModule moveNode = request.getNode();
        BaseModule previousNode = request.getPreviousNode();
        BaseModule nextNode = request.getNextNode();
        BaseModule parentModule = request.getParent();

        Project project = projectMapper.selectOneById(moveNode.getProjectId());
        String logContent;
        if (nextNode == null && previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName();
        } else if (nextNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + previousNode.getName() + Translator.get("file.log.next");
        } else if (previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + nextNode.getName() + Translator.get("file.log.previous");
        } else {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " +
                    previousNode.getName() + Translator.get("file.log.next") + " " + nextNode.getName() + Translator.get("file.log.previous");
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(moveNode.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(moveNode.getId())
                .content(logContent)
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
