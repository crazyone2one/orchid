package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.payload.dto.LogDTOBuilder;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.LogInsertModule;
import cn.master.backend.payload.request.plan.TestPlanBatchEditRequest;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLogService {


    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private TestPlanMapper testPlanMapper;

    public LogDTO scheduleLog(String id) {
        TestPlan testPlan = testPlanMapper.selectOneById(id);
        Project project = projectMapper.selectOneById(testPlan.getProjectId());
        return LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(testPlan))
                .sourceId(testPlan.getId())
                .content(Translator.get("test_plan_schedule") + ":" + testPlan.getName())
                .build().getLogDTO();
    }

    /**
     * 新增计划日志
     *
     * @param module        模块
     * @param operator      操作人
     * @param requestUrl    请求路径
     * @param requestMethod 请求方法
     */
    public void saveAddLog(TestPlan module, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(getLogModule(module))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 修改计划日志
     *
     * @param oldTestPlan   旧计划
     * @param newTestPlan   新计划
     * @param projectId     项目ID
     * @param operator      操作人
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     */
    public void saveUpdateLog(TestPlan oldTestPlan, TestPlan newTestPlan, String projectId, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(newTestPlan))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(newTestPlan.getId())
                .content(newTestPlan.getName())
                .originalValue(JSON.toJSONBytes(oldTestPlan))
                .modifiedValue(JSON.toJSONBytes(newTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 删除计划日志
     *
     * @param deleteTestPlan 删除计划
     * @param operator       操作人
     * @param requestUrl     请求URL
     * @param requestMethod  请求方法
     */
    public void saveDeleteLog(TestPlan deleteTestPlan, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectOneById(deleteTestPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteTestPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(getLogModule(deleteTestPlan))
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteTestPlan.getId())
                .content(deleteTestPlan.getName())
                .originalValue(JSON.toJSONBytes(deleteTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 归档日志
     *
     * @param id ID
     * @return 日志对象
     */
    public LogDTO archivedLog(String id) {
        TestPlan testPlan = testPlanMapper.selectOneById(id);
        LogDTO dto = new LogDTO(
                testPlan.getProjectId(),
                null,
                testPlan.getId(),
                null,
                OperationLogType.ARCHIVED.name(),
                getLogModule(testPlan),
                testPlan.getName());
        dto.setPath("/test-plan/archived");
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(testPlan));
        return dto;
    }

    /**
     * 复制日志
     */
    public void copyLog(TestPlan testPlan, String operator) {
        if (testPlan != null) {
            Project project = projectMapper.selectOneById(testPlan.getProjectId());
            LogDTO dto = new LogDTO(
                    testPlan.getProjectId(),
                    project.getOrganizationId(),
                    testPlan.getId(),
                    operator,
                    OperationLogType.ADD.name(),
                    getLogModule(testPlan),
                    testPlan.getName());
            dto.setPath("/test-plan/copy");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(testPlan));
            operationLogService.add(dto);
        }
    }

    /**
     * 保存批量日志
     *
     * @param plans         计划
     * @param operator      操作人
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     * @param requestType   请求类型
     */
    public void saveBatchLog(List<TestPlan> plans, String operator, String requestUrl, String requestMethod, String requestType) {
        if (CollectionUtils.isEmpty(plans)) {
            return;
        }
        Project project = projectMapper.selectOneById(plans.getFirst().getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (TestPlan plan : plans) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(plan.getProjectId())
                    .organizationId(project.getOrganizationId())
                    .type(requestType)
                    .module(getLogModule(plan))
                    .method(requestMethod)
                    .path(requestUrl)
                    .sourceId(plan.getId())
                    .content(plan.getName())
                    .originalValue(JSON.toJSONBytes(plan))
                    .createUser(operator)
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    /**
     * 批量编辑
     */
    public List<LogDTO> batchEditLog(TestPlanBatchEditRequest request) {
        // 测试计划不支持全选所有页
        List<String> ids = request.getSelectIds();
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<TestPlan> testPlans = QueryChain.of(TestPlan.class).where(TEST_PLAN.ID.in(ids).and(TEST_PLAN.PROJECT_ID.eq(request.getProjectId()))).list();
            Map<String, TestPlan> collect = testPlans.stream().collect(Collectors.toMap(TestPlan::getId, testPlan -> testPlan));
            ids.forEach(id -> {
                TestPlan testPlan = collect.get(id);
                LogDTO dto = new LogDTO(
                        testPlan.getProjectId(),
                        null,
                        testPlan.getId(),
                        null,
                        OperationLogType.UPDATE.name(),
                        getLogModule(testPlan),
                        testPlan.getName());
                dto.setPath("/test-plan/batch-edit");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(testPlan));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public void saveMoveLog(TestPlan testPlan, String moveId, LogInsertModule logInsertModule) {

        Project project = projectMapper.selectOneById(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(testPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(getLogModule(testPlan))
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(moveId)
                .content(Translator.get("log.test_plan.move.test_plan") + ":" + testPlan.getName() + StringUtils.SPACE)
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    private String getLogModule(TestPlan testPlan) {
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            return OperationLogModule.TEST_PLAN_TEST_PLAN;
        } else {
            return OperationLogModule.TEST_PLAN_TEST_PLAN_GROUP;
        }
    }
}