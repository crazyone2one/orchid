package cn.master.backend.service.impl;

import cn.master.backend.constants.ExecStatus;
import cn.master.backend.constants.ResultStatus;
import cn.master.backend.constants.ScheduleResourceType;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.*;
import cn.master.backend.mapper.BugRelationCaseMapper;
import cn.master.backend.payload.request.system.BaseScheduleConfigRequest;
import cn.master.backend.payload.response.plan.TestPlanBugPageResponse;
import cn.master.backend.payload.response.plan.TestPlanDetailResponse;
import cn.master.backend.payload.response.plan.TestPlanStatisticsResponse;
import cn.master.backend.service.BaseTestPlanService;
import cn.master.backend.service.TestPlanStatisticsService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.ScheduleUtils;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.ApiScenarioTableDef.API_SCENARIO;
import static cn.master.backend.entity.table.ApiTestCaseTableDef.API_TEST_CASE;
import static cn.master.backend.entity.table.BugRelationCaseTableDef.BUG_RELATION_CASE;
import static cn.master.backend.entity.table.BugTableDef.BUG;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.ScheduleTableDef.SCHEDULE;
import static cn.master.backend.entity.table.TestPlanApiCaseTableDef.TEST_PLAN_API_CASE;
import static cn.master.backend.entity.table.TestPlanApiScenarioTableDef.TEST_PLAN_API_SCENARIO;
import static cn.master.backend.entity.table.TestPlanConfigTableDef.TEST_PLAN_CONFIG;
import static cn.master.backend.entity.table.TestPlanFunctionalCaseTableDef.TEST_PLAN_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanStatisticsServiceImpl implements TestPlanStatisticsService {
    private final BaseTestPlanService baseTestPlanService;
    private final BugRelationCaseMapper bugRelationCaseMapper;

    @Override
    public List<TestPlanStatisticsResponse> calculateRate(List<String> paramIds) {
        List<TestPlan> paramTestPlanList = QueryChain.of(TestPlan.class).where(TEST_PLAN.ID.in(paramIds)).list();
        List<TestPlan> childrenTestPlan = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.in(paramIds)).list();
        paramTestPlanList.removeAll(childrenTestPlan);
        List<String> allTestPlanIdList = new ArrayList<>();
        allTestPlanIdList.addAll(paramTestPlanList.stream().map(TestPlan::getId).toList());
        allTestPlanIdList.addAll(childrenTestPlan.stream().map(TestPlan::getId).toList());
        Map<TestPlan, List<TestPlan>> groupTestPlanMap = new HashMap<>();
        for (TestPlan testPlan : paramTestPlanList) {
            List<TestPlan> children = new ArrayList<>();
            for (TestPlan child : childrenTestPlan) {
                if (StringUtils.equalsIgnoreCase(child.getGroupId(), testPlan.getId())) {
                    children.add(child);
                }
            }
            groupTestPlanMap.put(testPlan, children);
            childrenTestPlan.removeAll(children);
        }
        childrenTestPlan = null;
        paramTestPlanList = null;
        List<TestPlanStatisticsResponse> returnResponse = new ArrayList<>();

        // 计划的更多配置
        Map<String, TestPlanConfig> planConfigMap = selectConfig(allTestPlanIdList);
        // 关联的用例数据
        Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = getFunctionalCaseMapByPlanIds(allTestPlanIdList);
        Map<String, List<TestPlanApiCase>> planApiCaseMap = getApiCaseMapByPlanIds(allTestPlanIdList);
        Map<String, List<TestPlanApiScenario>> planApiScenarioMap = getApiScenarioByPlanIds(allTestPlanIdList);
        //查询定时任务
        Map<String, Schedule> scheduleMap = selectSchedule(allTestPlanIdList);

        groupTestPlanMap.forEach((rootPlan, children) -> {
            TestPlanStatisticsResponse rootResponse = genTestPlanStatisticsResponse(rootPlan, planConfigMap, planFunctionalCaseMap, planApiCaseMap, planApiScenarioMap, scheduleMap);
            rootResponse.setStatus(rootPlan.getStatus());

            List<TestPlanStatisticsResponse> childrenResponse = new ArrayList<>();
            if (!CollectionUtils.isEmpty(children)) {
                List<String> childStatus = new ArrayList<>();
                children.forEach(child -> {
                    TestPlanStatisticsResponse childResponse = this.genTestPlanStatisticsResponse(child, planConfigMap, planFunctionalCaseMap, planApiCaseMap, planApiScenarioMap, scheduleMap);
                    childResponse.setStatus(child.getStatus());

                    childResponse.calculateStatus();
                    childStatus.add(childResponse.getStatus());
                    //添加到rootResponse中
                    rootResponse.calculateAllNumber(childResponse);
                    childrenResponse.add(childResponse);
                });
                rootResponse.calculateCaseTotal();
                rootResponse.calculatePassRate();
                rootResponse.calculateExecuteRate();
                if (!StringUtils.equalsIgnoreCase(rootResponse.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                    rootResponse.setStatus(baseTestPlanService.calculateStatusByChildren(childStatus));
                }
            } else {
                rootResponse.calculateCaseTotal();
                rootResponse.calculatePassRate();
                rootResponse.calculateExecuteRate();
                rootResponse.calculateStatus();
            }
            returnResponse.add(rootResponse);
            returnResponse.addAll(childrenResponse);
        });
        return returnResponse;
    }

    @Override
    public void calculateCaseCount(List<TestPlanDetailResponse> response) {
        /*
         * 1. 查询计划下的用例数据集合
         */
        List<String> planIds = response.stream().map(TestPlanStatisticsResponse::getId).toList();
        Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = getFunctionalCaseMapByPlanIds(planIds);
        Map<String, List<TestPlanApiCase>> planApiCaseMap = getApiCaseMapByPlanIds(planIds);
        Map<String, List<TestPlanApiScenario>> planApiScenarioMap = getApiScenarioByPlanIds(planIds);
        // 计划-缺陷的关联数据
        List<TestPlanBugPageResponse> planBugs = countBugByIds(planIds);
        Map<String, List<TestPlanBugPageResponse>> planBugMap = planBugs.stream().collect(Collectors.groupingBy(TestPlanBugPageResponse::getTestPlanId));
        response.forEach(plan -> {
            List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(plan.getId());
            plan.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
            List<TestPlanApiCase> apiCases = planApiCaseMap.get(plan.getId());
            plan.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
            List<TestPlanApiScenario> apiScenarios = planApiScenarioMap.get(plan.getId());
            plan.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
            List<TestPlanBugPageResponse> bugs = planBugMap.get(plan.getId());
            plan.setBugCount(CollectionUtils.isNotEmpty(bugs) ? bugs.size() : 0);
            plan.setCaseTotal(plan.getFunctionalCaseCount() + plan.getApiCaseCount() + plan.getApiScenarioCount());
        });
    }

    private List<TestPlanBugPageResponse> countBugByIds(List<String> planIds) {
        return QueryChain.of(BugRelationCase.class)
                .select(BUG.ID, BUG.NUM, BUG.TITLE, BUG.DESCRIPTION.as("content"), BUG.HANDLE_USER, BUG.STATUS)
                .select(BUG.CREATE_USER, BUG.CREATE_TIME, BUG_RELATION_CASE.TEST_PLAN_ID)
                .from(BUG_RELATION_CASE)
                .join(BUG).on(BUG.ID.eq(BUG_RELATION_CASE.BUG_ID))
                .where(BUG_RELATION_CASE.TEST_PLAN_ID.in(planIds))
                .groupBy(BUG_RELATION_CASE.BUG_ID)
                .listAs(TestPlanBugPageResponse.class);
    }

    private TestPlanStatisticsResponse genTestPlanStatisticsResponse(TestPlan child,
                                                                     Map<String, TestPlanConfig> planConfigMap,
                                                                     Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap,
                                                                     Map<String, List<TestPlanApiCase>> planApiCaseMap,
                                                                     Map<String, List<TestPlanApiScenario>> planApiScenarioMap,
                                                                     Map<String, Schedule> scheduleMap) {
        String planId = child.getId();
        TestPlanStatisticsResponse statisticsResponse = new TestPlanStatisticsResponse();
        statisticsResponse.setId(planId);
        // 测试计划组没有测试计划配置。同理，也不用参与用例等数据的计算
        if (planConfigMap.containsKey(planId)) {
            statisticsResponse.setPassThreshold(planConfigMap.get(planId).getPassThreshold());

            List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(planId);
            List<TestPlanApiCase> apiCases = planApiCaseMap.get(planId);
            List<TestPlanApiScenario> apiScenarios = planApiScenarioMap.get(planId);


            // 功能用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> functionalCaseResultCountMap = countFunctionalCaseExecResultMap(functionalCases);
            // 接口用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> apiCaseResultCountMap = countApiTestCaseExecResultMap(apiCases);
            // 接口场景用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> apiScenarioResultCountMap = countApiScenarioExecResultMap(apiScenarios);

            // 用例数据汇总
            statisticsResponse.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
            statisticsResponse.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
            statisticsResponse.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
            statisticsResponse.setSuccessCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.SUCCESS.name()));
            statisticsResponse.setErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.ERROR.name()));
            statisticsResponse.setFakeErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.FAKE_ERROR.name()));
            statisticsResponse.setBlockCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.BLOCKED.name()));
            statisticsResponse.setPendingCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.PENDING.name()));
            statisticsResponse.calculateCaseTotal();
            statisticsResponse.calculatePassRate();
            statisticsResponse.calculateExecuteRate();
        }
        if (scheduleMap.containsKey(planId)) {
            Schedule schedule = scheduleMap.get(planId);
            BaseScheduleConfigRequest request = new BaseScheduleConfigRequest();
            request.setEnable(schedule.getEnable());
            request.setCron(schedule.getValue());
            request.setResourceId(planId);
            if (schedule.getConfig() != null) {
                request.setRunConfig(JSON.parseObject(schedule.getConfig(), Map.class));
            }
            statisticsResponse.setScheduleConfig(request);
            if (schedule.getEnable()) {
                statisticsResponse.setNextTriggerTime(ScheduleUtils.getNextTriggerTime(schedule.getValue()));
            }
        }
        return statisticsResponse;
    }

    private long countCaseMap(Map<String, Long> functionalCaseMap, Map<String, Long> apiCaseMap, Map<String, Long> apiScenarioMap, String countKey) {
        return (functionalCaseMap.containsKey(countKey) ? functionalCaseMap.get(countKey).intValue() : 0) +
                (apiCaseMap.containsKey(countKey) ? apiCaseMap.get(countKey).intValue() : 0) +
                (apiScenarioMap.containsKey(countKey) ? apiScenarioMap.get(countKey).intValue() : 0);
    }

    private Map<String, Long> countApiScenarioExecResultMap(List<TestPlanApiScenario> apiScenarios) {
        return CollectionUtils.isEmpty(apiScenarios) ? new HashMap<>(16) : apiScenarios.stream().collect(
                Collectors.groupingBy(apiScenario -> Optional.ofNullable(apiScenario.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
    }

    private Map<String, Long> countApiTestCaseExecResultMap(List<TestPlanApiCase> apiCases) {
        return CollectionUtils.isEmpty(apiCases) ? new HashMap<>(16) : apiCases.stream().collect(
                Collectors.groupingBy(apiCase -> Optional.ofNullable(apiCase.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
    }

    private Map<String, Long> countFunctionalCaseExecResultMap(List<TestPlanFunctionalCase> functionalCases) {
        return CollectionUtils.isEmpty(functionalCases) ? new HashMap<>(16) : functionalCases.stream().collect(
                Collectors.groupingBy(functionalCase -> Optional.ofNullable(functionalCase.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
    }

    private Map<String, Schedule> selectSchedule(List<String> planIds) {
        List<Schedule> schedules = QueryChain.of(Schedule.class).where(SCHEDULE.RESOURCE_ID.in(planIds)
                .and(SCHEDULE.RESOURCE_TYPE.eq(ScheduleResourceType.TEST_PLAN.name()))).list();
        return schedules.stream().collect(Collectors.toMap(Schedule::getResourceId, t -> t));
    }

    private Map<String, List<TestPlanApiScenario>> getApiScenarioByPlanIds(List<String> planIds) {
        List<TestPlanApiScenario> list = QueryChain.of(TestPlanApiScenario.class)
                .select(TEST_PLAN_API_SCENARIO.TEST_PLAN_ID, TEST_PLAN_API_SCENARIO.API_SCENARIO_ID, TEST_PLAN_API_SCENARIO.LAST_EXEC_RESULT)
                .from(TEST_PLAN_API_SCENARIO)
                .join(API_SCENARIO).on(API_SCENARIO.ID.eq(TEST_PLAN_API_SCENARIO.API_SCENARIO_ID))
                .where(TEST_PLAN_API_SCENARIO.TEST_PLAN_ID.in(planIds)).list();
        return list.stream().collect(Collectors.groupingBy(TestPlanApiScenario::getTestPlanId));
    }

    private Map<String, List<TestPlanApiCase>> getApiCaseMapByPlanIds(List<String> planIds) {
        List<TestPlanApiCase> list = QueryChain.of(TestPlanApiCase.class)
                .select(TEST_PLAN_API_CASE.TEST_PLAN_ID, TEST_PLAN_API_CASE.API_CASE_ID, TEST_PLAN_FUNCTIONAL_CASE.LAST_EXEC_RESULT)
                .from(TEST_PLAN_API_CASE)
                .join(API_TEST_CASE).on(API_TEST_CASE.ID.eq(TEST_PLAN_API_CASE.API_CASE_ID))
                .where(TEST_PLAN_API_CASE.TEST_PLAN_ID.in(planIds)).list();
        return list.stream().collect(Collectors.groupingBy(TestPlanApiCase::getTestPlanId));
    }

    private Map<String, List<TestPlanFunctionalCase>> getFunctionalCaseMapByPlanIds(List<String> testPlanIds) {
        List<TestPlanFunctionalCase> list = QueryChain.of(TestPlanFunctionalCase.class)
                .select(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID, TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID, TEST_PLAN_FUNCTIONAL_CASE.LAST_EXEC_RESULT)
                .from(TEST_PLAN_FUNCTIONAL_CASE)
                .join(FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID))
                .where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.in(testPlanIds)).list();
        return list.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
    }

    private Map<String, TestPlanConfig> selectConfig(List<String> testPlanIds) {
        List<TestPlanConfig> list = QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.in(testPlanIds)).list();
        return list.stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, p -> p));
    }
}
