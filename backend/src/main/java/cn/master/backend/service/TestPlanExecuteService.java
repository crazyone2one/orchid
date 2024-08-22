package cn.master.backend.service;

import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanExecutionQueue;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.payload.request.plan.TestPlanExecuteRequest;
import cn.master.backend.payload.request.plan.TestPlanReportGenRequest;
import cn.master.backend.util.JSON;
import cn.master.backend.util.LogUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.master.backend.service.TestPlanExecuteSupportService.QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanExecuteService {
    private final TestPlanExecuteSupportService testPlanExecuteSupportService;
    private final TestPlanMapper testPlanMapper;
    private final TestPlanService testPlanService;
    private final TestPlanReportService testPlanReportService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public String singleExecuteTestPlan(TestPlanExecuteRequest request, String reportId, String userId) {
        String queueId = IDGenerator.nextStr();
        TestPlanExecutionQueue singleExecuteRootQueue = new TestPlanExecutionQueue(
                0,
                userId,
                LocalDateTime.now(),
                queueId,
                QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE,
                null,
                null,
                request.getExecuteId(),
                request.getRunMode(),
                request.getExecutionSource(),
                reportId
        );

        testPlanExecuteSupportService.setRedisForList(
                testPlanExecuteSupportService.genQueueKey(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), List.of(JSON.toJSONString(singleExecuteRootQueue)));
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        LogUtils.info("测试计划（组）的单独执行start！计划报告[{}] , 资源ID[{}]", singleExecuteRootQueue.getPrepareReportId(), singleExecuteRootQueue.getSourceID());
        executeTestPlanOrGroup(nextQueue);
        return reportId;
    }

    private void executeTestPlanOrGroup(TestPlanExecutionQueue executionQueue) {
        TestPlan testPlan = testPlanMapper.selectOneById(executionQueue.getSourceID());
        if (Objects.isNull(testPlan) || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException("test_plan.error");
        }
        TestPlanReportGenRequest genReportRequest = new TestPlanReportGenRequest();
        genReportRequest.setTriggerMode(executionQueue.getExecutionSource());
        genReportRequest.setTestPlanId(executionQueue.getSourceID());
        genReportRequest.setProjectId(testPlan.getProjectId());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());
            // 预生成计划组报告
            Map<String, String> reportMap = testPlanReportService.genReportByExecution(executionQueue.getPrepareReportId(), genReportRequest, executionQueue.getCreateUser());

            long pos = 0;
        }
    }
}
