package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.TestPlanReport;
import cn.master.backend.handler.aspect.OperationLogAspect;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.mapper.TestPlanReportMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.plan.TestPlanReportDetailEditRequest;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanReportLogService {
    private final ProjectMapper projectMapper;
    private final TestPlanReportMapper testPlanReportMapper;
    private final OperationLogService operationLogService;
    public LogDTO deleteLog(String id) {
        TestPlanReport report = testPlanReportMapper.selectOneById(id);
        Project project = projectMapper.selectOneById(report.getProjectId());
        LogDTO dto = new LogDTO(
                report.getProjectId(),
                project.getOrganizationId(),
                report.getId(),
                null,
                OperationLogType.DELETE.name(),
                OperationLogModule.TEST_PLAN_REPORT,
                report.getName());

        dto.setPath(OperationLogAspect.getPath());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(report));
        return dto;
    }

    @SuppressWarnings("unused")
    public LogDTO updateLog(String id) {
        TestPlanReport report = testPlanReportMapper.selectOneById(id);
        Project project = projectMapper.selectOneById(report.getProjectId());
        LogDTO dto = new LogDTO(
                report.getProjectId(),
                project.getOrganizationId(),
                report.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.TEST_PLAN_REPORT,
                report.getName());

        dto.setPath(OperationLogAspect.getPath());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(report));
        return dto;
    }

    @SuppressWarnings("unused")
    public LogDTO renameLog(String id, Object name) {
        TestPlanReport report = testPlanReportMapper.selectOneById(id);
        Project project = projectMapper.selectOneById(report.getProjectId());
        LogDTO dto = new LogDTO(
                report.getProjectId(),
                project.getOrganizationId(),
                report.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.TEST_PLAN_REPORT,
                name.toString());

        dto.setPath(OperationLogAspect.getPath());
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(report));
        return dto;
    }

    @SuppressWarnings("unused")
    public LogDTO updateDetailLog(TestPlanReportDetailEditRequest request) {
        return updateLog(request.getId());
    }

    public void batchDeleteLog(List<String> ids, String userId, String projectId) {
        Project project = projectMapper.selectOneById(projectId);
        List<TestPlanReport> reports = testPlanReportMapper.selectListByIds(ids);
        List<LogDTO> logs = new ArrayList<>();
        reports.forEach(report -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    project.getOrganizationId(),
                    report.getId(),
                    userId,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.TEST_PLAN_REPORT,
                    report.getName());

            dto.setPath(OperationLogAspect.getPath());
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(report));
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }

    /**
     * 生成报告日志
     *
     * @param report    报告
     * @param userId    用户ID
     * @param projectId 项目ID
     */
    public void addLog(TestPlanReport report, String userId, String projectId) {
        Project project = projectMapper.selectOneById(projectId);
        LogDTO log = new LogDTO(
                projectId,
                project.getOrganizationId(),
                report.getId(),
                userId,
                OperationLogType.ADD.name(),
                report.getIntegrated() ? OperationLogModule.TEST_PLAN_GROUP_REPORT : OperationLogModule.TEST_PLAN_REPORT,
                report.getName());
        log.setMethod(HttpMethodConstants.POST.name());
        log.setOriginalValue(JSON.toJSONBytes(report));
        operationLogService.add(log);
    }
}
