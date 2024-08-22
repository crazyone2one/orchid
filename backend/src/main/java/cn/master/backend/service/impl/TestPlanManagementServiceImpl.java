package cn.master.backend.service.impl;

import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.constants.TestPlanResourceConfig;
import cn.master.backend.entity.Project;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.mapper.TestPlanModuleMapper;
import cn.master.backend.payload.dto.plan.TestPlanGroupCountDTO;
import cn.master.backend.payload.dto.plan.TestPlanResourceExecResultDTO;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.request.plan.TestPlanTableRequest;
import cn.master.backend.payload.response.plan.TestPlanResponse;
import cn.master.backend.service.BaseTestPlanService;
import cn.master.backend.service.TestPlanManagementService;
import cn.master.backend.service.TestPlanModuleService;
import cn.master.backend.service.TestPlanResourceService;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanManagementServiceImpl implements TestPlanManagementService {
    private final TestPlanMapper testPlanMapper;
    private final TestPlanModuleMapper testPlanModuleMapper;
    private final ApplicationContext applicationContext;
    private final BaseTestPlanService baseTestPlanService;
    private final TestPlanModuleService testPlanModuleService;

    @Override
    public void checkModuleIsOpen(String resourceId, String resourceType, List<String> moduleMenus) {
        Project project;
        if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN)) {
            TestPlan testPlan = testPlanMapper.selectOneById(resourceId);
            project = QueryChain.of(Project.class).where(Project::getId).eq(testPlan.getProjectId()).one();
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE)) {
            TestPlanModule testPlanModule = testPlanModuleMapper.selectOneById(resourceId);
            project = QueryChain.of(Project.class).where(Project::getId).eq(testPlanModule.getProjectId()).one();
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_PROJECT)) {
            project = QueryChain.of(Project.class).where(Project::getId).eq(resourceId).one();
        } else {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }
        if (Objects.isNull(project) || project.getModuleSetting().isEmpty()) {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }
        if (!new HashSet<>(project.getModuleSetting()).containsAll(moduleMenus)) {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }
    }

    @Override
    public Page<TestPlanResponse> page(TestPlanTableRequest request) {
        QueryChain<TestPlan> queryWrapper = QueryChain.of(TestPlan.class).where(TEST_PLAN.PROJECT_ID.eq(request.getProjectId()));
        extracted(request, queryWrapper);
        Page<TestPlanResponse> responsePage = testPlanMapper.paginateAs(request.getCurrent(), request.getPageSize(), queryWrapper, TestPlanResponse.class);
        handChildren(responsePage.getRecords(), request.getProjectId());
        return responsePage;
    }

    @Override
    public List<TestPlan> groupList(String projectId) {
        return QueryChain.of(TestPlan.class)
                .where(TEST_PLAN.TYPE.eq(TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                        .and(TEST_PLAN.PROJECT_ID.eq(projectId))
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                .list();
    }

    @Override
    public List<TestPlanResponse> selectByGroupId(String groupId) {
        return QueryChain.of(TestPlan.class)
                .where(TEST_PLAN.GROUP_ID.in(Collections.singletonList(groupId))).listAs(TestPlanResponse.class);
    }

    @Override
    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
        initDefaultFilter(request);
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        QueryChain<TestPlan> queryChain = QueryChain.of(TestPlan.class)
                .select(TEST_PLAN.MODULE_ID, QueryMethods.count(TEST_PLAN.ID).as("dataCount"))
                .from(TEST_PLAN)
                .where(TEST_PLAN.PROJECT_ID.eq(request.getProjectId()));
        extracted(request, queryChain);
        List<ModuleCountDTO> moduleCountDTOList = queryChain
                .groupBy(TEST_PLAN.MODULE_ID)
                .listAs(ModuleCountDTO.class);
        //extTestPlanMapper.countModuleIdByConditions(request);
        Map<String, Long> moduleCountMap = testPlanModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        long allCount = 0;
        for (ModuleCountDTO item : moduleCountDTOList) {
            allCount += item.getDataCount();
        }
        moduleCountMap.put("all", allCount);
        return moduleCountMap;
    }

    private static void extracted(TestPlanTableRequest request, QueryChain<TestPlan> queryChain) {
        queryChain.and(TEST_PLAN.MODULE_ID.in(request.getModuleIds()));
        switch (request.getType()) {
            case "ALL" -> queryChain.and(TEST_PLAN.GROUP_ID.eq("NONE"));
            case "TEST_PLAN" -> queryChain.and(TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("TEST_PLAN")));
            case "GROUP" -> queryChain.and(TEST_PLAN.GROUP_ID.eq("NONE").and(TEST_PLAN.TYPE.eq("GROUP")));
        }
        queryChain.and(TEST_PLAN.NAME.like(request.getKeyword())
                        .or(TEST_PLAN.NUM.like(request.getKeyword()))
                        .or(TEST_PLAN.TAGS.like(request.getKeyword()))
                        .or(TEST_PLAN.ID.in(request.getKeywordFilterIds())))
                .and(TEST_PLAN.ID.in(request.getInnerIds()))
                .orderBy(TEST_PLAN.POS.desc(), TEST_PLAN.ID.desc());
    }

    private void initDefaultFilter(TestPlanTableRequest request) {
        List<String> defaultStatusList = new ArrayList<>();
        defaultStatusList.add(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        if (request.getFilter() == null || !request.getFilter().containsKey("status")) {
            if (request.getFilter() == null) {
                request.setFilter(new HashMap<>() {{
                    this.put("status", defaultStatusList);
                }});
            } else {
                request.getFilter().put("status", defaultStatusList);
            }
        } else if (!request.getFilter().get("status").contains(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            List<String> statusList = request.getFilter().get("status");
            request.getFilter().put("status", defaultStatusList);
            //目前未归档的测试计划只有3中类型。所以这里判断如果是3个的话等于直接查询未归档
            if (statusList.size() < 3) {
                request.setInnerIds(selectTestPlanIdByProjectIdAndStatus(request.getProjectId(), statusList));
            }
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            List<String> groupIdList = QueryChain.of(TestPlan.class)
                    .select(TEST_PLAN.GROUP_ID).from(TEST_PLAN)
                    .where(TEST_PLAN.PROJECT_ID.eq(request.getProjectId()))
                    .and(TEST_PLAN.NAME.like(request.getKeyword())
                            .or(TEST_PLAN.NUM.like(request.getKeyword()))
                            .or(TEST_PLAN.TAGS.like(request.getKeyword())))
                    .listAs(String.class);
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                request.setKeywordFilterIds(groupIdList);
            }
        }
    }

    private List<String> selectTestPlanIdByProjectIdAndStatus(String projectId, @NotEmpty List<String> statusList) {
        List<String> innerIdList = new ArrayList<>();
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
        List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
        beansOfType.forEach((k, v) -> execResults.addAll(v.selectDistinctExecResultByProjectId(projectId)));
        Map<String, Map<String, List<String>>> testPlanExecMap = baseTestPlanService.parseExecResult(execResults);
        Map<String, Long> groupCountMap = QueryChain.of(TestPlan.class)
                .select(TEST_PLAN.GROUP_ID, QueryMethods.count(TEST_PLAN.ID).as("count"))
                .from(TEST_PLAN)
                .where(TEST_PLAN.PROJECT_ID.eq(projectId)
                        .and(TEST_PLAN.STATUS.ne("ARCHIVED")).and(TEST_PLAN.GROUP_ID.ne("NONE")))
                .groupBy(TEST_PLAN.GROUP_ID)
                .listAs(TestPlanGroupCountDTO.class)
                .stream().collect(Collectors.toMap(TestPlanGroupCountDTO::getGroupId, TestPlanGroupCountDTO::getCount));
        List<String> completedTestPlanIds = new ArrayList<>();
        List<String> preparedTestPlanIds = new ArrayList<>();
        List<String> underwayTestPlanIds = new ArrayList<>();
        testPlanExecMap.forEach((groupId, planMap) -> {
            if (StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                this.filterTestPlanIdWithStatus(planMap, completedTestPlanIds, preparedTestPlanIds, underwayTestPlanIds);
            } else {
                long itemPlanCount = groupCountMap.getOrDefault(groupId, 0L);
                List<String> itemStatusList = new ArrayList<>();
                if (itemPlanCount > planMap.size()) {
                    // 存在未执行或者没有用例的测试计划。 此时这种测试计划的状态为未开始
                    itemStatusList.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED);
                }
                planMap.forEach((planId, resultList) -> {
                    itemStatusList.add(baseTestPlanService.calculateTestPlanStatus(resultList));
                });
                String groupStatus = baseTestPlanService.calculateStatusByChildren(itemStatusList);
                if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                    completedTestPlanIds.add(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                    underwayTestPlanIds.add(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                    preparedTestPlanIds.add(groupId);
                }
            }
        });

        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            // 已完成
            innerIdList.addAll(completedTestPlanIds);
        }

        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            // 进行中
            innerIdList.addAll(underwayTestPlanIds);
        }
        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            // 未开始   有一些测试计划/计划组没有用例 / 测试计划， 在上面的计算中无法过滤。所以用排除法机型处理
            List<String> withoutList = new ArrayList<>();
            withoutList.addAll(completedTestPlanIds);
            withoutList.addAll(underwayTestPlanIds);
            innerIdList.addAll(QueryChain.of(TestPlan.class)
                    .select(TEST_PLAN.ID).from(TEST_PLAN)
                    .where(TEST_PLAN.PROJECT_ID.eq(projectId)
                            .and(TEST_PLAN.STATUS.ne("ARCHIVED"))
                            .and(TEST_PLAN.GROUP_ID.eq("NONE")))
                    .and(TEST_PLAN.ID.notIn(withoutList))
                    .listAs(String.class));
        }
        return innerIdList;
    }

    private void filterTestPlanIdWithStatus(Map<String, List<String>> testPlanExecMap, List<String> completedTestPlanIds, List<String> preparedTestPlanIds, List<String> underwayTestPlanIds) {
        testPlanExecMap.forEach((planId, resultList) -> {
            String result = baseTestPlanService.calculateTestPlanStatus(resultList);
            if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                completedTestPlanIds.add(planId);
            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                underwayTestPlanIds.add(planId);
            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                preparedTestPlanIds.add(planId);
            }
        });
    }

    private void handChildren(List<TestPlanResponse> testPlanResponses, String projectId) {
        List<String> groupIds = testPlanResponses.stream().filter(item -> StringUtils.equals(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)).map(TestPlanResponse::getId).toList();
        if (CollectionUtils.isNotEmpty(groupIds)) {
            List<TestPlanResponse> childrenList = QueryChain.of(TestPlan.class)
                    .where(TEST_PLAN.GROUP_ID.in(groupIds)).listAs(TestPlanResponse.class);
            Map<String, List<TestPlanResponse>> collect = childrenList.stream().collect(Collectors.groupingBy(TestPlanResponse::getGroupId));
            testPlanResponses.forEach(item -> {
                if (collect.containsKey(item.getId())) {
                    //存在子节点
                    List<TestPlanResponse> list = collect.get(item.getId());
                    item.setChildren(list);
                    item.setChildrenCount(list.size());
                }
            });
        }
    }
}
