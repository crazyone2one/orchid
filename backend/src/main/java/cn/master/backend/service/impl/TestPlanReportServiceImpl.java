package cn.master.backend.service.impl;

import cn.master.backend.constants.CaseType;
import cn.master.backend.constants.ExecStatus;
import cn.master.backend.constants.ResultStatus;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.*;
import cn.master.backend.payload.dto.SelectOption;
import cn.master.backend.payload.dto.plan.*;
import cn.master.backend.payload.request.plan.TestPlanReportGenRequest;
import cn.master.backend.service.BugCommonService;
import cn.master.backend.service.TestPlanReportService;
import cn.master.backend.service.log.TestPlanReportLogService;
import cn.master.backend.util.*;
import com.google.common.collect.Maps;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.ApiDefinitionModuleTableDef.API_DEFINITION_MODULE;
import static cn.master.backend.entity.table.ApiScenarioModuleTableDef.API_SCENARIO_MODULE;
import static cn.master.backend.entity.table.ApiScenarioTableDef.API_SCENARIO;
import static cn.master.backend.entity.table.BugRelationCaseTableDef.BUG_RELATION_CASE;
import static cn.master.backend.entity.table.BugTableDef.BUG;
import static cn.master.backend.entity.table.CustomFieldOptionTableDef.CUSTOM_FIELD_OPTION;
import static cn.master.backend.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.backend.entity.table.FunctionalCaseCustomFieldTableDef.FUNCTIONAL_CASE_CUSTOM_FIELD;
import static cn.master.backend.entity.table.FunctionalCaseModuleTableDef.FUNCTIONAL_CASE_MODULE;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.TestPlanApiScenarioTableDef.TEST_PLAN_API_SCENARIO;
import static cn.master.backend.entity.table.TestPlanFunctionalCaseTableDef.TEST_PLAN_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.TestPlanReportApiCaseTableDef.TEST_PLAN_REPORT_API_CASE;
import static cn.master.backend.entity.table.TestPlanReportFunctionCaseTableDef.TEST_PLAN_REPORT_FUNCTION_CASE;
import static cn.master.backend.entity.table.TestPlanReportTableDef.TEST_PLAN_REPORT;
import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * 测试计划报告 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
@RequiredArgsConstructor
public class TestPlanReportServiceImpl extends ServiceImpl<TestPlanReportMapper, TestPlanReport> implements TestPlanReportService {
    private final TestPlanReportMapper testPlanReportMapper;
    private final TestPlanReportSummaryMapper testPlanReportSummaryMapper;
    private final TestPlanReportLogService testPlanReportLogService;
    private final TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
    private final TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
    private final TestPlanReportBugMapper testPlanReportBugMapper;
    private final TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    private final BugCommonService bugCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Map<String, String> genReportByExecution(String prepareReportId, TestPlanReportGenRequest request, String currentUser) {
        return genReport(prepareReportId, request, false, currentUser, null);
    }

    @Override
    public void summaryGroupReport(String reportId) {
        List<TestPlanReportSummary> testPlanReportSummaries = QueryChain.of(TestPlanReportSummary.class).where(TestPlanReportSummary::getTestPlanReportId).eq(reportId).list();
        if (CollectionUtils.isEmpty(testPlanReportSummaries)) {
            // 报告详情不存在
            return;
        }
        TestPlanReportSummary groupSummary = testPlanReportSummaries.getFirst();
        List<TestPlanReport> testPlanReports = QueryChain.of(TestPlanReport.class).where(TestPlanReport::getParentId).eq(reportId)
                .and(TestPlanReport::getIntegrated).eq(false).list();
        if (CollectionUtils.isEmpty(testPlanReports)) {
            // 不存在子报告, 不需要汇总数据
            return;
        }
        List<String> ids = testPlanReports.stream().map(TestPlanReport::getId).toList();
        List<TestPlanReportFunctionCase> reportFunctionCases = QueryChain.of(TestPlanReportFunctionCase.class).where(TestPlanReportFunctionCase::getTestPlanReportId).in(ids).list();
        if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
            groupSummary.setFunctionalCaseCount((long) reportFunctionCases.size());
            reportFunctionCases.forEach(reportFunctionCase -> {
                reportFunctionCase.setTestPlanReportId(reportId);
            });
            // 插入计划组报告, 功能用例关联数据
            testPlanReportFunctionCaseMapper.insertBatch(reportFunctionCases);
        }
        // 接口用例
        List<TestPlanReportApiCase> reportApiCases = QueryChain.of(TestPlanReportApiCase.class).where(TestPlanReportApiCase::getTestPlanReportId).in(ids).list();
        if (CollectionUtils.isNotEmpty(reportApiCases)) {
            groupSummary.setApiCaseCount((long) reportApiCases.size());
            reportApiCases.forEach(reportApiCase -> {
                reportApiCase.setTestPlanReportId(reportId);
            });
            // 插入计划组报告, 接口用例关联数据
            testPlanReportApiCaseMapper.insertBatch(reportApiCases);
        }
        List<TestPlanReportBug> reportBugs = QueryChain.of(TestPlanReportBug.class).where(TestPlanReportBug::getTestPlanReportId).in(ids).list();
        if (CollectionUtils.isNotEmpty(reportBugs)) {
            groupSummary.setBugCount((long) reportBugs.size());
            reportBugs.forEach(reportBug -> {
                //reportBug.setId(IDGenerator.nextStr());
                reportBug.setTestPlanReportId(reportId);
            });
            // 插入计划组关联用例缺陷数据
            testPlanReportBugMapper.insertBatch(reportBugs);
        }
        List<TestPlanReportSummary> summaryList = QueryChain.of(TestPlanReportSummary.class).where(TestPlanReportSummary::getTestPlanReportId).in(ids).list();
        List<CaseCount> functionalCaseCountList = new ArrayList<>();
        List<CaseCount> apiCaseCountList = new ArrayList<>();
        List<CaseCount> scenarioCountList = new ArrayList<>();
        List<CaseCount> executeCountList = new ArrayList<>();
        summaryList.forEach(summary -> {
            CaseCount functionalCaseCount = summary.getFunctionalExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getFunctionalExecuteResult()), CaseCount.class);
            CaseCount apiCaseCount = summary.getApiExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getApiExecuteResult()), CaseCount.class);
            CaseCount scenarioCount = summary.getScenarioExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getScenarioExecuteResult()), CaseCount.class);
            CaseCount executeCount = summary.getExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getExecuteResult()), CaseCount.class);
            functionalCaseCountList.add(functionalCaseCount);
            apiCaseCountList.add(apiCaseCount);
            scenarioCountList.add(scenarioCount);
            executeCountList.add(executeCount);
        });
        // 入库组汇总数据 => 报告详情表
        groupSummary.setFunctionalExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(functionalCaseCountList)));
        groupSummary.setApiExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(apiCaseCountList)));
        groupSummary.setScenarioExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(scenarioCountList)));
        groupSummary.setExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(executeCountList)));
        testPlanReportSummaryMapper.update(groupSummary);
    }

    @Override
    public void summaryPlanReport(String reportId) {
        List<TestPlanReportSummary> testPlanReportSummaries = QueryChain.of(TestPlanReportSummary.class).where(TestPlanReportSummary::getTestPlanReportId).eq(reportId).list();
        if (CollectionUtils.isEmpty(testPlanReportSummaries)) {
            // 报告详情不存在
            return;
        }
        // 功能用例
        List<CaseStatusCountMap> functionalCountMapList = QueryChain.of(TestPlanReportFunctionCase.class)
                .select(QueryMethods.ifNull("tprfc.function_case_execute_result", "PENDING"))
                .select(QueryMethods.count(TEST_PLAN_REPORT_FUNCTION_CASE.ID).as("count"))
                .from(TEST_PLAN_REPORT_FUNCTION_CASE.as("tprfc"))
                .where(TEST_PLAN_REPORT_FUNCTION_CASE.TEST_PLAN_REPORT_ID.eq(reportId))
                .groupBy(TEST_PLAN_REPORT_FUNCTION_CASE.FUNCTION_CASE_EXECUTE_RESULT)
                .listAs(CaseStatusCountMap.class);
        CaseCount functionalCaseCount = countMap(functionalCountMapList);
        // 接口用例
        List<CaseStatusCountMap> apiCountMapList = QueryChain.of(TestPlanReportApiCase.class)
                .select(TEST_PLAN_REPORT_API_CASE.ID.as("testPlanApiCaseId"))

                .from(TEST_PLAN_REPORT_API_CASE.as("tpac"))

                .listAs(CaseStatusCountMap.class);
        // todo
        CaseCount apiCaseCount = countMap(apiCountMapList);
        // 场景用例
        List<CaseStatusCountMap> scenarioCountMapList = new ArrayList<>();
        CaseCount scenarioCaseCount = countMap(scenarioCountMapList);
        // 规划整体的汇总数据
        CaseCount summaryCount = CaseCount.builder().success(functionalCaseCount.getSuccess() + apiCaseCount.getSuccess() + scenarioCaseCount.getSuccess())
                .error(functionalCaseCount.getError() + apiCaseCount.getError() + scenarioCaseCount.getError())
                .block(functionalCaseCount.getBlock() + apiCaseCount.getBlock() + scenarioCaseCount.getBlock())
                .pending(functionalCaseCount.getPending() + apiCaseCount.getPending() + scenarioCaseCount.getPending())
                .fakeError(functionalCaseCount.getFakeError() + apiCaseCount.getFakeError() + scenarioCaseCount.getFakeError()).build();

        // 入库汇总数据 => 报告详情表
        TestPlanReportSummary reportSummary = testPlanReportSummaries.getFirst();
        reportSummary.setFunctionalExecuteResult(JSON.toJSONBytes(functionalCaseCount));
        reportSummary.setApiExecuteResult(JSON.toJSONBytes(apiCaseCount));
        reportSummary.setScenarioExecuteResult(JSON.toJSONBytes(scenarioCaseCount));
        reportSummary.setExecuteResult(JSON.toJSONBytes(summaryCount));
        testPlanReportSummaryMapper.update(reportSummary);
    }

    @Override
    public void postHandleReport(TestPlanReportPostParam postParam, boolean useManual) {
        /*
         * 处理报告(执行状态, 结束时间)
         */
        TestPlanReport planReport = checkReport(postParam.getReportId());
        BeanUtils.copyProperties(postParam, planReport);
        /*
         * 计算报告通过率, 并对比阈值生成报告结果状态
         */
        TestPlanReportSummary reportSummary = QueryChain.of(TestPlanReportSummary.class).where(TestPlanReportSummary::getTestPlanReportId).eq(postParam.getReportId()).one();
        // 用例总数
        long caseTotal = reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount();
        CaseCount summaryCount = JSON.parseObject(new String(reportSummary.getExecuteResult()), CaseCount.class);
        planReport.setExecuteRate(RateCalculateUtils.divWithPrecision(((int) caseTotal - summaryCount.getPending()), (int) caseTotal, 2));
        planReport.setPassRate(RateCalculateUtils.divWithPrecision(summaryCount.getSuccess(), (int) caseTotal, 2));
        if (planReport.getIntegrated()) {
            // 计划组的(执行)结果状态: 子计划全部成功 ? 成功 : 失败
            long count = QueryChain.of(TestPlanReport.class).where(TEST_PLAN_REPORT.PARENT_ID.eq(postParam.getReportId())
                    .and(TEST_PLAN_REPORT.INTEGRATED.eq(false))
                    .and(TEST_PLAN_REPORT.RESULT_STATUS.ne(ResultStatus.SUCCESS.name()))).count();
            planReport.setResultStatus(count == 0 ? ResultStatus.SUCCESS.name() : ResultStatus.ERROR.name());
        } else {
            // 计划的(执行)结果状态: 通过率 >= 阈值 ? 成功 : 失败
            planReport.setResultStatus(planReport.getPassRate() >= planReport.getPassThreshold() ? ResultStatus.SUCCESS.name() : ResultStatus.ERROR.name());
        }

        testPlanReportMapper.update(planReport);

        // todo 发送计划执行通知
        //if (!useManual) {
        //    testPlanSendNoticeService.sendExecuteNotice(planReport.getCreateUser(), planReport.getTestPlanId(), planReport);
        //}
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTestPlanIds(List<String> testPlanIds) {
        if (!testPlanIds.isEmpty()) {
            List<TestPlanReport> testPlanReports = queryChain().where(TEST_PLAN_REPORT.TEST_PLAN_ID.in(testPlanIds)).list();
            if (!testPlanReports.isEmpty()) {
                Map<String, TestPlanReport> reportMap = testPlanReports.stream().collect(Collectors.toMap(TestPlanReport::getId, r -> r));
                List<String> reportIdList = testPlanReports.stream().map(TestPlanReport::getId).toList();
                reportIdList.forEach(reportId -> {
                    TestPlanReport report = reportMap.get(reportId);
                    if (StringUtils.isNotBlank(report.getParentId()) && !report.getIntegrated() && !report.getDeleted()) {
                        mapper.delete(report);
                    } else {
                        mapper.deleteByQuery(queryChain().where(TEST_PLAN_REPORT.ID.eq(reportId).and(TEST_PLAN_REPORT.PARENT_ID.eq(reportId))));
                    }
                });
                deleteTestPlanReportBlobs(reportIdList);
            }
        }
    }

    private void deleteTestPlanReportBlobs(List<String> reportIdList) {

        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanReportSummaryMapper.deleteByQuery(QueryChain.of(TestPlanReportSummary.class)
                        .where(TestPlanReportSummary::getTestPlanReportId).in(reportIdList))
        );

        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanReportFunctionCaseMapper.deleteByQuery(QueryChain.of(TestPlanReportFunctionCase.class)
                        .where(TestPlanReportFunctionCase::getTestPlanReportId).in(reportIdList))
        );

        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanReportApiCaseMapper.deleteByQuery(QueryChain.of(TestPlanReportApiCase.class)
                        .where(TestPlanReportApiCase::getTestPlanReportId).in(reportIdList))
        );
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanReportApiScenarioMapper.deleteByQuery(QueryChain.of(TestPlanReportApiScenario.class)
                        .where(TestPlanReportApiScenario::getTestPlanReportId).in(reportIdList))
        );
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanReportBugMapper.deleteByQuery(QueryChain.of(TestPlanReportBug.class)
                        .where(TestPlanReportBug::getTestPlanReportId).in(reportIdList))
        );
    }

    private TestPlanReport checkReport(String reportId) {
        return QueryChain.of(TestPlanReport.class).where(TestPlanReport::getId).eq(reportId).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("test_plan_report_not_exist")));
    }

    private CaseCount countMap(List<CaseStatusCountMap> resultMapList) {
        CaseCount caseCount = new CaseCount();
        Map<String, Long> resultMap = resultMapList.stream().collect(Collectors.toMap(CaseStatusCountMap::getStatus, CaseStatusCountMap::getCount));
        caseCount.setSuccess(resultMap.getOrDefault(ResultStatus.SUCCESS.name(), 0L).intValue());
        caseCount.setError(resultMap.getOrDefault(ResultStatus.ERROR.name(), 0L).intValue());
        caseCount.setPending(resultMap.getOrDefault(ExecStatus.PENDING.name(), 0L).intValue() + resultMap.getOrDefault(ExecStatus.STOPPED.name(), 0L).intValue());
        caseCount.setBlock(resultMap.getOrDefault(ResultStatus.BLOCKED.name(), 0L).intValue());
        caseCount.setFakeError(resultMap.getOrDefault(ResultStatus.FAKE_ERROR.name(), 0L).intValue());
        return caseCount;
    }

    private Map<String, String> genReport(String prepareReportId, TestPlanReportGenRequest request, boolean manual, String currentUser, String manualReportName) {
        Map<String, String> preReportMap = Maps.newHashMapWithExpectedSize(8);
        TestPlanReportManualParam reportManualParam = TestPlanReportManualParam.builder().manualName(manualReportName).targetId(request.getTestPlanId()).build();
        try {
            // 所有计划
            List<TestPlan> plans = getPlans(request.getTestPlanId());

            /*
             * 1. 准备报告生成参数
             * 2. 预生成报告
             * 3. 汇总报告数据 {执行时跳过}
             * 3. 报告后置处理 (计算通过率, 执行率, 执行状态...) {执行时跳过}
             */
            List<String> childPlanIds = plans.stream().filter(plan -> StringUtils.equals(plan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)).map(TestPlan::getId).toList();

            boolean isGroupReports = plans.size() > 1;
            plans.forEach(plan -> {
                request.setTestPlanId(plan.getId());
                TestPlanReportGenPreParam genPreParam = buildReportGenParam(request, plan, prepareReportId);
                if (!manual) {
                    // 不是手动保存的测试计划报告，不存储startTime
                    genPreParam.setStartTime(null);
                }
                genPreParam.setUseManual(manual);
                // 如果是测试计划的独立报告，使用参数中的预生成的报告id。否则只有测试计划组报告使用该id
                String prepareItemReportId = isGroupReports ? IDGenerator.nextStr() : prepareReportId;
                TestPlanReport preReport = preGenReport(prepareItemReportId, genPreParam, currentUser, childPlanIds, reportManualParam);
                if (manual) {
                    // 汇总
                    if (genPreParam.getIntegrated()) {
                        summaryGroupReport(preReport.getId());
                    } else {
                        summaryPlanReport(preReport.getId());
                    }
                    // 手动生成的报告, 汇总结束后直接进行后置处理
                    TestPlanReportPostParam postParam = new TestPlanReportPostParam();
                    postParam.setReportId(preReport.getId());
                    // 手动生成报告, 执行状态为已完成, 执行及结束时间为当前时间
                    postParam.setExecuteTime(LocalDateTime.now());
                    postParam.setEndTime(LocalDateTime.now());
                    postParam.setExecStatus(ExecStatus.COMPLETED.name());
                    postHandleReport(postParam, true);
                }
                preReportMap.put(plan.getId(), preReport.getId());
            });
        } catch (Exception e) {
            LogUtils.error("Generate report exception: " + e.getMessage());
        }

        return preReportMap;
    }

    private TestPlanReportGenPreParam buildReportGenParam(TestPlanReportGenRequest genRequest, TestPlan testPlan, String groupReportId) {
        TestPlanReportGenPreParam genPreParam = new TestPlanReportGenPreParam();
        BeanUtils.copyProperties(genRequest, genPreParam);
        genPreParam.setTestPlanName(testPlan.getName());
        genPreParam.setStartTime(System.currentTimeMillis());
        // 报告预生成时, 执行状态为未执行, 结果状态为'-'
        genPreParam.setExecStatus(ExecStatus.PENDING.name());
        genPreParam.setResultStatus("-");
        // 是否集成报告(计划组报告), 目前根据是否计划组来区分
        genPreParam.setIntegrated(StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP));
        genPreParam.setGroupReportId(groupReportId);
        return genPreParam;
    }

    private List<TestPlan> getPlans(String groupOrPlanId) {
        TestPlan testPlan = checkPlan(groupOrPlanId);
        List<TestPlan> plans = new ArrayList<>();
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            // 计划组
            List<TestPlan> testPlans = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.eq(groupOrPlanId)).list();
            plans.addAll(testPlans);
        }
        // 保证最后一条为计划组
        plans.addLast(testPlan);
        return plans;
    }

    private TestPlan checkPlan(String planId) {
        return QueryChain.of(TestPlan.class).where(TEST_PLAN.ID.eq(planId)).oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("test_plan_not_exist")));
    }

    private TestPlanReport preGenReport(String prepareId, TestPlanReportGenPreParam genParam, String currentUser, List<String> childPlanIds,
                                        TestPlanReportManualParam reportManualParam) {
        TestPlanConfig config = QueryChain.of(TestPlanConfig.class).where(TestPlanConfig::getTestPlanId).in(genParam.getTestPlanId()).one();
        TestPlanReport report = new TestPlanReport();
        BeanUtils.copyProperties(genParam, report);
        report.setId(genParam.getIntegrated() ? genParam.getGroupReportId() : prepareId);
        report.setName((StringUtils.equals(genParam.getTestPlanId(), reportManualParam.getTargetId()) && StringUtils.isNotBlank(reportManualParam.getManualName())) ?
                reportManualParam.getManualName() : genParam.getTestPlanName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setCreateUser(currentUser);
        report.setDeleted(false);
        report.setPassThreshold(config == null ? null : config.getPassThreshold());
        report.setParentId(genParam.getGroupReportId());
        report.setTestPlanName(genParam.getTestPlanName());
        testPlanReportMapper.insert(report);
        TestPlanReportDetailCaseDTO reportCaseDetail;
        if (!genParam.getIntegrated()) {
            // 生成独立报告的关联数据
            reportCaseDetail = genReportDetail(genParam, report);
        } else {
            // 计划组报告暂不统计各用例类型, 汇总时再入库
            reportCaseDetail = TestPlanReportDetailCaseDTO.builder().build();
        }
        // 报告统计内容
        TestPlanReportSummary reportSummary = new TestPlanReportSummary();
        reportSummary.setTestPlanReportId(report.getId());
        reportSummary.setFunctionalCaseCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getFunctionCases()) ? 0 : reportCaseDetail.getFunctionCases().size()));
        reportSummary.setApiCaseCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getApiCases()) ? 0 : reportCaseDetail.getApiCases().size()));
        reportSummary.setApiScenarioCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getApiScenarios()) ? 0 : reportCaseDetail.getApiScenarios().size()));
        reportSummary.setBugCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getBugs()) ? 0 : reportCaseDetail.getBugs().size()));
        reportSummary.setPlanCount(genParam.getIntegrated() ? (long) childPlanIds.size() : 0);
        testPlanReportSummaryMapper.insert(reportSummary);
        // 报告日志
        testPlanReportLogService.addLog(report, currentUser, genParam.getProjectId());
        return report;
    }

    private TestPlanReportDetailCaseDTO genReportDetail(TestPlanReportGenPreParam genParam, TestPlanReport report) {
        List<TestPlanReportFunctionCase> reportFunctionCases = getPlanExecuteCases(genParam.getTestPlanId());
        if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
            // 用例等级
            List<String> ids = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getFunctionCaseId).distinct().toList();
            List<SelectOption> options = getCasePriorityByIds(ids);
            Map<String, String> casePriorityMap = options.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
            // 用例模块
            List<String> moduleIds = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getFunctionCaseModule).filter(Objects::nonNull).toList();
            Map<String, String> moduleMap = getModuleMapByIds(moduleIds, CaseType.FUNCTIONAL_CASE.getKey());
            // 关联的功能用例最新一次执行历史
            List<String> relateIds = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getTestPlanFunctionCaseId).toList();
            List<TestPlanCaseExecuteHistory> functionalExecHisList = QueryChain.of(TestPlanCaseExecuteHistory.class)
                    .where(TestPlanCaseExecuteHistory::getTestPlanCaseId).in(relateIds).list();
            Map<String, List<TestPlanCaseExecuteHistory>> functionalExecMap = functionalExecHisList.stream().collect(Collectors.groupingBy(TestPlanCaseExecuteHistory::getTestPlanCaseId));

            for (TestPlanReportFunctionCase reportFunctionalCase : reportFunctionCases) {
                reportFunctionalCase.setTestPlanReportId(report.getId());
                reportFunctionalCase.setTestPlanName(genParam.getTestPlanName());
                if (reportFunctionalCase.getFunctionCaseModule() != null) {
                    reportFunctionalCase.setFunctionCaseModule(moduleMap.getOrDefault(reportFunctionalCase.getFunctionCaseModule(), reportFunctionalCase.getFunctionCaseModule()));
                }
                reportFunctionalCase.setFunctionCasePriority(casePriorityMap.get(reportFunctionalCase.getFunctionCaseId()));
                List<TestPlanCaseExecuteHistory> hisList = functionalExecMap.get(reportFunctionalCase.getTestPlanFunctionCaseId());
                if (CollectionUtils.isNotEmpty(hisList)) {
                    Optional<String> lastExecuteHisOpt = hisList.stream().sorted(Comparator.comparing(TestPlanCaseExecuteHistory::getCreateTime).reversed()).map(TestPlanCaseExecuteHistory::getId).findFirst();
                    reportFunctionalCase.setFunctionCaseExecuteReportId(lastExecuteHisOpt.orElse(null));
                } else {
                    reportFunctionalCase.setFunctionCaseExecuteReportId(null);
                }
            }

            // 插入计划功能用例关联数据 -> 报告内容
            testPlanReportFunctionCaseMapper.insertBatch(reportFunctionCases);
        }

        // 接口用例
        List<TestPlanReportApiCase> reportApiCases = getPlanApiExecuteCases(genParam.getTestPlanId());
        if (CollectionUtils.isNotEmpty(reportApiCases)) {
            // 用例模块
            List<String> moduleIds = reportApiCases.stream().map(TestPlanReportApiCase::getApiCaseModule).filter(Objects::nonNull).toList();
            Map<String, String> moduleMap = getModuleMapByIds(moduleIds, CaseType.API_CASE.getKey());

            for (TestPlanReportApiCase reportApiCase : reportApiCases) {
                reportApiCase.setTestPlanReportId(report.getId());
                reportApiCase.setTestPlanName(genParam.getTestPlanName());
                if (reportApiCase.getApiCaseModule() != null) {
                    reportApiCase.setApiCaseModule(moduleMap.getOrDefault(reportApiCase.getApiCaseModule(), reportApiCase.getApiCaseModule()));
                }
                // 根据不超过数据库字段最大长度压缩模块名
                reportApiCase.setApiCaseModule(ServiceUtils.compressName(reportApiCase.getApiCaseModule(), 450));
                if (!genParam.getUseManual()) {
                    // 接口执行时才更新结果
                    reportApiCase.setApiCaseExecuteResult(null);
                    reportApiCase.setApiCaseExecuteUser(null);
                    reportApiCase.setApiCaseExecuteReportId(IDGenerator.nextStr());
                }
            }
            // 插入计划接口用例关联数据 -> 报告内容
            testPlanReportApiCaseMapper.insertBatch(reportApiCases);
        }

        // 场景用例
        List<TestPlanReportApiScenario> reportApiScenarios = getPlanApiScenarioExecuteCases(genParam.getTestPlanId());
        if (CollectionUtils.isNotEmpty(reportApiScenarios)) {
            // 用例模块
            List<String> moduleIds = reportApiScenarios.stream().map(TestPlanReportApiScenario::getApiScenarioModule).filter(Objects::nonNull).toList();
            Map<String, String> moduleMap = getModuleMapByIds(moduleIds, CaseType.SCENARIO_CASE.getKey());

            for (TestPlanReportApiScenario reportApiScenario : reportApiScenarios) {
                reportApiScenario.setTestPlanReportId(report.getId());
                reportApiScenario.setTestPlanName(genParam.getTestPlanName());
                if (reportApiScenario.getApiScenarioModule() != null) {
                    reportApiScenario.setApiScenarioModule(moduleMap.getOrDefault(reportApiScenario.getApiScenarioModule(), reportApiScenario.getApiScenarioModule()));
                }
                // 根据不超过数据库字段最大长度压缩模块名
                reportApiScenario.setApiScenarioModule(ServiceUtils.compressName(reportApiScenario.getApiScenarioModule(), 450));
                if (!genParam.getUseManual()) {
                    // 接口执行时才更新结果
                    reportApiScenario.setApiScenarioExecuteResult(null);
                    reportApiScenario.setApiScenarioExecuteUser(null);
                    reportApiScenario.setApiScenarioExecuteReportId(IDGenerator.nextStr());
                }
            }
            // 插入计划场景用例关联数据 -> 报告内容
            testPlanReportApiScenarioMapper.insertBatch(reportApiScenarios);
        }

        // 计划报告缺陷内容
        List<TestPlanReportBug> reportBugs = getPlanBugs(genParam.getTestPlanId());
        if (CollectionUtils.isNotEmpty(reportBugs)) {
            // MS处理人会与第三方的值冲突, 分开查询
            List<SelectOption> headerOptions = bugCommonService.getHeaderHandlerOption(genParam.getProjectId());
            Map<String, String> headerHandleUserMap = headerOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
            List<SelectOption> localOptions = bugCommonService.getLocalHandlerOption(genParam.getProjectId());
            Map<String, String> localHandleUserMap = localOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
            Map<String, String> allStatusMap = bugCommonService.getAllStatusMap(genParam.getProjectId());
            reportBugs.forEach(reportBug -> {
                reportBug.setId(IDGenerator.nextStr());
                reportBug.setTestPlanReportId(report.getId());
                reportBug.setBugHandleUser(headerHandleUserMap.containsKey(reportBug.getBugHandleUser()) ?
                        headerHandleUserMap.get(reportBug.getBugHandleUser()) : localHandleUserMap.get(reportBug.getBugHandleUser()));
                reportBug.setBugStatus(allStatusMap.get(reportBug.getBugStatus()));
            });
            // 插入计划关联用例缺陷数据(去重) -> 报告内容
            testPlanReportBugMapper.insertBatch(reportBugs);
        }
        return TestPlanReportDetailCaseDTO.builder()
                .functionCases(reportFunctionCases).apiCases(reportApiCases).apiScenarios(reportApiScenarios).bugs(reportBugs).build();
    }

    private List<TestPlanReportBug> getPlanBugs(String testPlanId) {
        return QueryChain.of(BugRelationCase.class)
                .select(QueryMethods.distinct(BUG_RELATION_CASE.BUG_ID, BUG.NUM.as("bugNum"),
                        BUG.TITLE.as("bugTitle"),
                        BUG.STATUS.as("bugStatus"), BUG.HANDLE_USER.as("bugHandleUser"),
                        QueryMethods.count(BUG_RELATION_CASE.ID).as("bugCaseCount")))
                .from(BUG_RELATION_CASE)
                .join(BUG).on(BUG_RELATION_CASE.BUG_ID.eq(BUG.ID))
                .where(BUG_RELATION_CASE.TEST_PLAN_ID.eq(testPlanId))
                .groupBy(BUG_RELATION_CASE.BUG_ID)
                .listAs(TestPlanReportBug.class);
    }

    private List<TestPlanReportApiScenario> getPlanApiScenarioExecuteCases(String testPlanId) {
        return QueryChain.of(TestPlanApiScenario.class)
                .select(TEST_PLAN_API_SCENARIO.ID.as("testPlanApiScenarioId"), API_SCENARIO.ID.as("apiScenarioId"),
                        API_SCENARIO.NUM.as("apiScenarioNum"), API_SCENARIO.NAME.as("apiScenarioName"),
                        API_SCENARIO.PRIORITY.as("apiScenarioPriority"),
                        TEST_PLAN_API_SCENARIO.TEST_PLAN_COLLECTION_ID.as("testPlanCollectionId"),
                        TEST_PLAN_API_SCENARIO.GROUPED, TEST_PLAN_API_SCENARIO.ENVIRONMENT_ID,
                        TEST_PLAN_API_SCENARIO.EXECUTE_USER.as("apiScenarioExecuteUser"),
                        TEST_PLAN_API_SCENARIO.LAST_EXEC_REPORT_ID.as("apiScenarioExecuteReportId"))
                .select(QueryMethods.count(BUG_RELATION_CASE.ID)).as("functionCaseBugCount")
                .select(QueryMethods.if_(API_SCENARIO.MODULE_ID.eq("root"), "未规划用例", String.valueOf(API_SCENARIO.MODULE_ID)).as("apiScenarioModule"))
                .select(QueryMethods.ifNull(String.valueOf(TEST_PLAN_API_SCENARIO.LAST_EXEC_RESULT), "PENDING").as("apiScenarioExecuteResult"))
                .select(TEST_PLAN_API_SCENARIO.POS)
                .from(TEST_PLAN_API_SCENARIO)
                .join(API_SCENARIO).on(TEST_PLAN_API_SCENARIO.API_SCENARIO_ID.eq(API_SCENARIO.ID))
                .leftJoin(API_SCENARIO_MODULE).on(API_SCENARIO_MODULE.ID.eq(API_SCENARIO.MODULE_ID))
                .where(TEST_PLAN_API_SCENARIO.TEST_PLAN_ID.eq(testPlanId))
                .groupBy(TEST_PLAN_API_SCENARIO.ID)
                .listAs(TestPlanReportApiScenario.class);
    }

    private List<TestPlanReportApiCase> getPlanApiExecuteCases(String testPlanId) {
        // todo
        return null;
    }

    private Map<String, String> getModuleMapByIds(List<String> moduleIds, String caseType) {
        if (moduleIds.isEmpty()) {
            return Map.of();
        }
        List<TestPlanBaseModule> modules = new ArrayList<>();
        if (StringUtils.equals(caseType, CaseType.FUNCTIONAL_CASE.getKey())) {
            modules = QueryChain.of(FunctionalCaseModule.class).where(FUNCTIONAL_CASE_MODULE.ID.in(modules)).listAs(TestPlanBaseModule.class);
        } else if (StringUtils.equals(caseType, CaseType.API_CASE.getKey())) {
            modules = QueryChain.of(ApiDefinitionModule.class).where(API_DEFINITION_MODULE.ID.in(modules)).listAs(TestPlanBaseModule.class);
        } else if (StringUtils.equals(caseType, CaseType.SCENARIO_CASE.getKey())) {
            modules = QueryChain.of(ApiScenarioModule.class).where(API_SCENARIO_MODULE.ID.in(modules)).listAs(TestPlanBaseModule.class);
        }
        return modules.stream().collect(Collectors.toMap(TestPlanBaseModule::getId, TestPlanBaseModule::getName));
    }

    private List<SelectOption> getCasePriorityByIds(List<String> ids) {
        return QueryChain.of(FunctionalCase.class)
                .select(QueryMethods.distinct(FUNCTIONAL_CASE.ID.as("value"), FUNCTIONAL_CASE_CUSTOM_FIELD.VALUE.as("text")))
                .from(FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE_CUSTOM_FIELD).on(FUNCTIONAL_CASE.ID.eq(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID))
                .leftJoin(CUSTOM_FIELD).on(CUSTOM_FIELD.ID.eq(FUNCTIONAL_CASE_CUSTOM_FIELD.FIELD_ID))
                .leftJoin(CUSTOM_FIELD_OPTION).on(CUSTOM_FIELD_OPTION.FIELD_ID.eq(CUSTOM_FIELD.ID))
                .where(FUNCTIONAL_CASE.NAME.eq("functional_priority").and(FUNCTIONAL_CASE.ID.in(ids)))
                .listAs(SelectOption.class);
    }

    private List<TestPlanReportFunctionCase> getPlanExecuteCases(String testPlanId) {
        return QueryChain.of(TestPlanFunctionalCase.class)
                .select(TEST_PLAN_FUNCTIONAL_CASE.ID.as("testPlanFunctionCaseId"), FUNCTIONAL_CASE.ID.as("functionCaseId"),
                        FUNCTIONAL_CASE.NUM.as("functionCaseNum"), FUNCTIONAL_CASE.NAME.as("functionCaseName"),
                        TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_COLLECTION_ID.as("testPlanCollectionId"),
                        TEST_PLAN_FUNCTIONAL_CASE.EXECUTE_USER.as("functionCaseExecuteUser"))
                .select(QueryMethods.count(BUG_RELATION_CASE.ID)).as("functionCaseBugCount")
                .select(QueryMethods.if_(FUNCTIONAL_CASE.MODULE_ID.eq("root"), "未规划用例", String.valueOf(FUNCTIONAL_CASE.MODULE_ID)).as("functionCaseModule"))
                .select(QueryMethods.ifNull(String.valueOf(TEST_PLAN_FUNCTIONAL_CASE.LAST_EXEC_RESULT), "PENDING").as("functionCaseExecuteResult"))
                .select(TEST_PLAN_FUNCTIONAL_CASE.POS)
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .join(FUNCTIONAL_CASE).on(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .leftJoin(FUNCTIONAL_CASE_MODULE).on(FUNCTIONAL_CASE.MODULE_ID.eq(FUNCTIONAL_CASE_MODULE.ID))
                .leftJoin(BUG_RELATION_CASE).on(TEST_PLAN_FUNCTIONAL_CASE.ID.eq(BUG_RELATION_CASE.TEST_PLAN_CASE_ID))
                .where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.eq(testPlanId))
                .groupBy(TEST_PLAN_FUNCTIONAL_CASE.ID)
                .listAs(TestPlanReportFunctionCase.class);
    }

}
