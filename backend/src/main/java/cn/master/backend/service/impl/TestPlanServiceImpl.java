package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.job.TestPlanScheduleJob;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.*;
import cn.master.backend.payload.dto.plan.TestPlanExecuteHisDTO;
import cn.master.backend.payload.dto.system.LogInsertModule;
import cn.master.backend.payload.request.plan.*;
import cn.master.backend.payload.request.project.ProjectApplicationRequest;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.payload.response.plan.TestPlanDetailResponse;
import cn.master.backend.payload.response.plan.TestPlanOperationResponse;
import cn.master.backend.payload.response.plan.TestPlanStatisticsResponse;
import cn.master.backend.service.*;
import cn.master.backend.service.log.TestPlanLogService;
import cn.master.backend.util.*;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.TestPlanFollowerTableDef.TEST_PLAN_FOLLOWER;
import static cn.master.backend.entity.table.TestPlanModuleTableDef.TEST_PLAN_MODULE;
import static cn.master.backend.entity.table.TestPlanReportTableDef.TEST_PLAN_REPORT;
import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 测试计划 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
@RequiredArgsConstructor
public class TestPlanServiceImpl extends BaseTestPlanServiceImpl implements TestPlanService {
    private final TestPlanLogService testPlanLogService;
    private final TestPlanGroupService testPlanGroupService;
    private final TestPlanConfigMapper testPlanConfigMapper;
    private final ProjectApplicationService projectApplicationService;
    private final TestPlanCollectionMapper testPlanCollectionMapper;
    private final ScheduleService scheduleService;
    private final TestPlanFollowerMapper testPlanFollowerMapper;
    private final TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    private final TestPlanStatisticsService testPlanStatisticsService;
    private final ScheduleMapper scheduleMapper;
    private final TestPlanModuleMapper testPlanModuleMapper;
    private final ApplicationContext applicationContext;
    private final TestPlanBatchOperationService testPlanBatchOperationService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        List<String> deleteIdList = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            List<TestPlan> deleteTestPlanList = mapper.selectListByIds(deleteIdList);
            if (CollectionUtils.isNotEmpty(deleteTestPlanList)) {
                List<String> testPlanGroupList = new ArrayList<>();
                List<String> testPlanIdList = new ArrayList<>();
                for (TestPlan testPlan : deleteTestPlanList) {
                    if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                        testPlanGroupList.add(testPlan.getId());
                    } else {
                        testPlanIdList.add(testPlan.getId());
                    }
                }
                // todo testPlanSendNoticeService.batchSendNotice(request.getProjectId(), deleteIdList, userMapper.selectByPrimaryKey(operator), NoticeConstants.Event.DELETE);
                deleteByList(testPlanIdList);
                // 计划组的删除暂时预留
                deleteGroupByList(testPlanGroupList);
                //记录日志
                testPlanLogService.saveBatchLog(deleteTestPlanList, operator, requestUrl, requestMethod, OperationLogType.DELETE.name());
            }
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPlan add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = savePlanDTO(testPlanCreateRequest, operator);
        initDefaultPlanCollection(testPlan.getId(), operator);
        testPlanLogService.saveAddLog(testPlan, operator, requestUrl, requestMethod);
        return testPlan;
    }

    @Override
    public List<TestPlanCollection> initDefaultPlanCollection(String planId, String currentUser) {
        TestPlan testPlan = mapper.selectOneById(planId);
        ProjectApplicationRequest projectApplicationRequest = new ProjectApplicationRequest();
        projectApplicationRequest.setProjectId(testPlan.getProjectId());
        projectApplicationRequest.setType("apiTest");
        Map<String, Object> configMap = projectApplicationService.get(projectApplicationRequest, Arrays.stream(ProjectApplicationType.API.values()).map(ProjectApplicationType.API::name).collect(Collectors.toList()));
        // 批量插入测试集
        List<TestPlanCollection> collections = new ArrayList<>();
        TestPlanCollection defaultCollection = new TestPlanCollection();
        defaultCollection.setTestPlanId(planId);
        defaultCollection.setExecuteMethod(ExecuteMethod.SERIAL.name());
        defaultCollection.setExtended(true);
        defaultCollection.setGrouped(false);
        defaultCollection.setEnvironmentId("NONE");
        defaultCollection.setTestResourcePoolId(configMap.getOrDefault(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), StringUtils.EMPTY).toString());
        defaultCollection.setRetryOnFail(false);
        defaultCollection.setStopOnFail(false);
        defaultCollection.setCreateUser(currentUser);
        long initPos = 1L;
        for (CaseType caseType : CaseType.values()) {
            TestPlanCollection parentCollection = new TestPlanCollection();
            BeanUtils.copyProperties(defaultCollection, parentCollection);
            //parentCollection.setId(IDGenerator.nextStr());
            parentCollection.setParentId(TestPlanConstants.DEFAULT_PARENT_ID);
            parentCollection.setName(caseType.getType());
            parentCollection.setType(caseType.getKey());
            parentCollection.setPos(initPos << 12);
            //testPlanCollectionMapper.insert(parentCollection);
            collections.add(parentCollection);

            //TestPlanCollection childCollection = new TestPlanCollection();
            //BeanUtils.copyProperties(defaultCollection, childCollection);
            //childCollection.setId(IDGenerator.nextStr());
            //childCollection.setParentId(parentCollection.getId());
            //childCollection.setName(caseType.getPlanDefaultCollection());
            //childCollection.setType(caseType.getKey());
            //childCollection.setPos(1L << 12);
            //collections.add(childCollection);
            //
            //parentCollection.setChildren(List.of(childCollection));
            //// 更新pos
            //initPos++;
            //collections.add(parentCollection);
        }
        //testPlanCollectionMapper.insertBatch(collections);
        //List<String> list = collections.stream().map(TestPlanCollection::getId).toList();
        collections.forEach(testPlanCollectionMapper::insert);
        return collections;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPlan update(TestPlanUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        checkTestPlanNotArchived(request.getId());
        TestPlan originalTestPlan = mapper.selectOneById(request.getId());
        TestPlan updateTestPlan = new TestPlan();
        updateTestPlan.setId(request.getId());
        updateTestPlan.setName(request.getName());
        updateTestPlan.setPlannedStartTime(request.getPlannedStartTime());
        updateTestPlan.setPlannedEndTime(request.getPlannedEndTime());
        updateTestPlan.setDescription(request.getDescription());
        //判断有没有用户组的变化
        if (StringUtils.isNotBlank(request.getGroupId()) && !StringUtils.equalsIgnoreCase(request.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            if (!StringUtils.equalsIgnoreCase(originalTestPlan.getGroupId(), request.getGroupId())) {
                //用户更换了测试计划组
                TestPlan testPlanGroup = testPlanGroupService.validateGroupCapacity(request.getGroupId(), 1);
                updateTestPlan.setGroupId(testPlanGroup.getId());
                deleteScheduleConfig(request.getId());
                updateTestPlan.setPos(testPlanGroupService.getNextOrder(request.getGroupId()));
                updateTestPlan.setModuleId(testPlanGroup.getModuleId());
            }
        } else {
            updateTestPlan.setGroupId(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            if (!StringUtils.equalsIgnoreCase(originalTestPlan.getGroupId(), request.getGroupId())) {
                //移出了测试计划组
                deleteScheduleConfig(request.getId());
                updateTestPlan.setPos(testPlanGroupService.getNextOrder(updateTestPlan.getGroupId()));
            }
        }
        //判断有没有模块的变化
        if (StringUtils.isNotBlank(request.getModuleId())) {
            if (!StringUtils.equalsIgnoreCase(request.getModuleId(), originalTestPlan.getModuleId())) {
                //检查模块的合法性
                checkModule(request.getModuleId());
                updateTestPlan.setModuleId(request.getModuleId());
            }
        }
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            List<String> tags = new ArrayList<>(request.getTags());
            updateTestPlan.setTags(ServiceUtils.parseTags(tags));
        } else {
            updateTestPlan.setTags(new ArrayList<>());
        }
        mapper.update(updateTestPlan);
        TestPlan testPlan = mapper.selectOneById(updateTestPlan.getId());
        if (!ObjectUtils.allNull(request.getAutomaticStatusUpdate(), request.getRepeatCase(), request.getPassThreshold(), request.getTestPlanning())) {
            TestPlanConfig testPlanConfig = new TestPlanConfig();
            testPlanConfig.setTestPlanId(request.getId());
            testPlanConfig.setAutomaticStatusUpdate(request.getAutomaticStatusUpdate());
            testPlanConfig.setRepeatCase(request.getRepeatCase());
            testPlanConfig.setPassThreshold(request.getPassThreshold());
            testPlanConfigMapper.update(testPlanConfig);
        }

        testPlanLogService.saveUpdateLog(testPlan, mapper.selectOneById(request.getId()), testPlan.getProjectId(), userId, requestUrl, requestMethod);
        return updateTestPlan;
    }

    @Override
    public void checkTestPlanNotArchived(String testPlanId) {
        boolean exists = queryChain().where(TEST_PLAN.ID.eq(testPlanId)
                .and(TEST_PLAN.STATUS.eq(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED))).exists();
        if (exists) {
            throw new MSException(Translator.get("test_plan.is.archived"));
        }
    }

    @Override
    public void deleteScheduleConfig(String testPlanId) {
        scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId));
        queryChain().where(TEST_PLAN.GROUP_ID.eq(testPlanId)).list()
                .forEach(testPlan -> scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId)));
    }

    @Override
    public List<TestPlan> selectNotArchivedChildren(String testPlanGroupId) {
        return queryChain()
                .where(TEST_PLAN.GROUP_ID.eq(testPlanGroupId)
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                .list();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void delete(String id, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = mapper.selectOneById(id);
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            this.deleteGroupByList(Collections.singletonList(testPlan.getId()));
        } else {
            LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteById(id));
            //级联删除
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
            assert testPlanReportService != null;
            cascadeDeleteTestPlanIds(Collections.singletonList(id), testPlanReportService);
        }
        //记录日志
        testPlanLogService.saveDeleteLog(testPlan, operator, requestUrl, requestMethod);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void editFollower(String testPlanId, String userId) {
        QueryChain<TestPlanFollower> queryChain = QueryChain.of(TestPlanFollower.class).where(TEST_PLAN_FOLLOWER.TEST_PLAN_ID.eq(testPlanId)
                .and(TEST_PLAN_FOLLOWER.USER_ID.eq(userId)));
        if (queryChain.count() > 0) {
            LogicDeleteManager.execWithoutLogicDelete(() -> testPlanFollowerMapper.deleteByQuery(queryChain));
        } else {
            TestPlanFollower testPlanFollower = new TestPlanFollower();
            testPlanFollower.setTestPlanId(testPlanId);
            testPlanFollower.setUserId(userId);
            testPlanFollowerMapper.insert(testPlanFollower);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void archived(String testPlanId, String userId) {
        TestPlan testPlan = mapper.selectOneById(testPlanId);
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //判断当前计划组下是否都已完成 (由于算法原因，只需要校验当前测试计划组即可）
            if (!isTestPlanCompleted(testPlanId)) {
                throw new MSException(Translator.get("test_plan.group.not_plan"));
            }
            //测试计划组归档
            updateCompletedGroupStatus(testPlan.getId(), userId);
            //关闭定时任务
            deleteScheduleConfig(testPlan.getId());
        } else if (isTestPlanCompleted(testPlanId) && StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            mapper.update(testPlan);
            //关闭定时任务
            deleteScheduleConfig(testPlan.getId());
        } else {
            throw new MSException(Translator.get("test_plan.cannot.archived"));
        }
    }

    @Override
    public TestPlanDetailResponse detail(String id, String userId) {
        TestPlan testPlan = mapper.selectOneById(id);
        TestPlanDetailResponse response = new TestPlanDetailResponse();
        String moduleName = Translator.get("unplanned.plan");
        if (!ModuleConstants.DEFAULT_NODE_ID.equals(testPlan.getModuleId())) {
            TestPlanModule module = testPlanModuleMapper.selectOneById(testPlan.getModuleId());
            moduleName = module == null ? Translator.get("unplanned.plan") : module.getName();
            response.setModuleId(module == null ? ModuleConstants.DEFAULT_NODE_ID : module.getId());
        } else {
            response.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        }

        //计划组只有几个参数
        response.setId(testPlan.getId());
        response.setNum(testPlan.getNum());
        response.setStatus(testPlan.getStatus());
        response.setName(testPlan.getName());
        response.setTags(testPlan.getTags());
        response.setModuleName(moduleName);
        response.setDescription(testPlan.getDescription());
        response.setType(testPlan.getType());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            //计划的 其他参数
            getGroupName(response, testPlan);
            response.setPlannedStartTime(testPlan.getPlannedStartTime());
            response.setPlannedEndTime(testPlan.getPlannedEndTime());
            getOtherConfig(response, testPlan);
            testPlanStatisticsService.calculateCaseCount(List.of(response));
        }
        // 是否定时任务
        QueryChain<Schedule> scheduleQueryChain = QueryChain.of(Schedule.class).where(Schedule::getResourceId).eq(id)
                .and(Schedule::getResourceType).eq("TEST_PLAN");
        response.setUseSchedule(scheduleMapper.selectCountByQuery(scheduleQueryChain) > 0);

        //是否关注计划
        Boolean isFollow = checkIsFollowCase(id, userId);
        response.setFollowFlag(isFollow);

        // 是否计划已初始化默认测试集
        QueryChain<TestPlanCollection> queryChain = QueryChain.of(TestPlanCollection.class).where(TestPlanCollection::getTestPlanId).eq(id);
        if (queryChain.count() == 0) {
            List<TestPlanCollection> collections = initDefaultPlanCollection(id, userId);
            initResourceDefaultCollection(id, collections);
        }
        return response;
    }

    @Override
    public String copy(String planId, String userId) {
        TestPlan testPlan = mapper.selectOneById(planId);
        TestPlan copyPlan = null;
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            copyPlan = testPlanBatchOperationService.copyPlanGroup(testPlan, testPlan.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT, userId);
        } else {
            copyPlan = testPlanBatchOperationService.copyPlan(testPlan, testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, userId);
        }
        testPlanLogService.copyLog(copyPlan, userId);
        return copyPlan.getId();
    }

    @Override
    public void filterArchivedIds(TestPlanBatchRequest request) {
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            request.setSelectIds(
                    queryChain().select(TEST_PLAN.ID).from(TEST_PLAN)
                            .where(TEST_PLAN.ID.in(request.getSelectIds())
                                    .and(TEST_PLAN.STATUS.ne("ARCHIVED")))
                            .listAs(String.class)
            );
        }
    }

    @Override
    public long batchCopy(TestPlanBatchRequest request, String userId, String url, String method) {
        List<String> copyIds = request.getSelectIds();
        long copyCount = 0;
        if (CollectionUtils.isNotEmpty(copyIds)) {
            List<TestPlan> originalTestPlanList = mapper.selectListByIds(copyIds);
            //批量复制时，不允许存在测试计划组下的测试计划。
            originalTestPlanList = originalTestPlanList.stream().filter(item -> StringUtils.equalsIgnoreCase(item.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))
                    .collect(Collectors.toList());
            //日志
            if (CollectionUtils.isNotEmpty(originalTestPlanList)) {
                List<TestPlan> copyPlanList = testPlanBatchOperationService.batchCopy(originalTestPlanList, request.getTargetId(), request.getMoveType(), userId);
                copyCount = copyPlanList.size();
                testPlanLogService.saveBatchLog(copyPlanList, userId, url, method, OperationLogType.ADD.name());
            }

        }
        return copyCount;
    }

    @Override
    public long batchMove(TestPlanBatchRequest request, String userId, String operationUrl, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> moveIds = request.getSelectIds();

        long moveCount = 0;
        if (CollectionUtils.isNotEmpty(moveIds)) {
            List<TestPlan> moveTestPlanList = mapper.selectListByIds(moveIds);

            //判断移动的是测试计划组还是模块
            if (StringUtils.equalsIgnoreCase(request.getMoveType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                moveTestPlanList = moveTestPlanList.stream().filter(
                        item -> StringUtils.equalsIgnoreCase(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !StringUtils.equalsIgnoreCase(item.getGroupId(), request.getTargetId())
                ).collect(Collectors.toList());
                if (!StringUtils.equalsIgnoreCase(request.getTargetId(), TestPlanConstants.DEFAULT_PARENT_ID)) {
                    testPlanGroupService.validateGroupCapacity(request.getTargetId(), moveTestPlanList.size());
                }
                moveCount = testPlanBatchOperationService.batchMoveGroup(moveTestPlanList, request.getTargetId(), userId);
                //移动到计划组后要删除定时任务
                moveTestPlanList.forEach(item -> this.deleteScheduleConfig(item.getId()));
            } else {
                moveCount = testPlanBatchOperationService.batchMoveModule(moveTestPlanList, request.getTargetId(), userId);
            }
            //日志
            if (CollectionUtils.isNotEmpty(moveTestPlanList)) {
                testPlanLogService.saveBatchLog(moveTestPlanList, userId, operationUrl, method, OperationLogType.UPDATE.name());
            }
        }
        return moveCount;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchArchived(TestPlanBatchProcessRequest request, String operator) {
        List<String> batchArchivedIds = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(batchArchivedIds)) {
            List<TestPlan> list = queryChain().where(TEST_PLAN.ID.in(batchArchivedIds)
                    .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED))).list();
            List<TestPlan> testPlanList = list.stream().filter(
                    testPlan -> StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                            || (StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))
            ).toList();
            List<TestPlan> archivedPlanGroupList = new ArrayList<>();
            testPlanList.forEach(item -> {
                boolean result = archived(item, operator);
                if (result) {
                    archivedPlanGroupList.add(item);
                }
            });
            //日志
            testPlanLogService.saveBatchLog(archivedPlanGroupList, operator, "/test-plan/batch-archived", HttpMethodConstants.POST.name(), OperationLogType.ARCHIVED.name());
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean archived(TestPlan testPlan, String userId) {
        if (!this.isTestPlanCompleted(testPlan.getId())) {
            return false;
        }
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //测试计划组归档
            updateCompletedGroupStatus(testPlan.getId(), userId);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
            return true;
        } else if (StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            mapper.update(testPlan);
            //关闭定时任务
            deleteScheduleConfig(testPlan.getId());
            return true;
        }
        return false;
    }

    @Override
    public void filterArchivedIds(TestPlanBatchEditRequest request) {
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            request.setSelectIds(queryChain().select(TEST_PLAN.ID).from(TEST_PLAN)
                    .where(TEST_PLAN.ID.in(request.getSelectIds())
                            .and(TEST_PLAN.STATUS.ne("ARCHIVED")))
                    .listAs(String.class));
        }
    }

    @Override
    public void batchEdit(TestPlanBatchEditRequest request, String userId) {
        // 目前计划的批量操作不支持全选所有页
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            if (StringUtils.equalsIgnoreCase(request.getEditColumn(), "SCHEDULE")) {
                List<TestPlan> testPlanList = queryChain().where(TEST_PLAN.ID.in(ids)
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED))).list();
                //批量编辑定时任务
                for (TestPlan testPlan : testPlanList) {
                    scheduleService.updateIfExist(testPlan.getId(), request.isScheduleOpen(), TestPlanScheduleJob.getJobKey(testPlan.getId()),
                            TestPlanScheduleJob.getTriggerKey(testPlan.getId()), TestPlanScheduleJob.class, userId);
                }
            } else {
                //默认编辑tags
                //User user = userMapper.selectByPrimaryKey(userId);
                handleTags(request, userId, ids);
                //testPlanSendNoticeService.batchSendNotice(request.getProjectId(), ids, user, NoticeConstants.Event.UPDATE);
            }
        }
    }

    @Override
    public TestPlanOperationResponse sort(PosRequest request, LogInsertModule logInsertModule) {
        testPlanGroupService.sort(request);
        testPlanLogService.saveMoveLog(mapper.selectOneById(request.getMoveId()), request.getMoveId(), logInsertModule);
        return new TestPlanOperationResponse(1);
    }

    @Override
    public Page<TestPlanExecuteHisDTO> listHis(TestPlanExecuteHisPageRequest request) {
        QueryChain<TestPlanReport> queryChain = QueryChain.of(TestPlanReport.class)
                .select(TEST_PLAN_REPORT.ID, TEST_PLAN_REPORT.TRIGGER_MODE,
                        TEST_PLAN_REPORT.EXEC_STATUS, TEST_PLAN_REPORT.RESULT_STATUS,
                        USER.NAME.as("operationUser"), TEST_PLAN_REPORT.START_TIME,
                        TEST_PLAN_REPORT.END_TIME)
                .select(QueryMethods.dateFormat(TEST_PLAN_REPORT.CREATE_TIME, "%Y%m%d%H%i%s"))
                .from(TEST_PLAN_REPORT)
                .leftJoin(USER).on(USER.ID.eq(TEST_PLAN_REPORT.CREATE_USER))
                .where(TEST_PLAN_REPORT.TEST_PLAN_ID.eq(request.getTestPlanId()))
                .orderBy(TEST_PLAN_REPORT.CREATE_TIME.desc());
        if (!request.getFilter().isEmpty()) {
            Map<String, List<String>> filter = request.getFilter();
            for (String key : filter.keySet()) {
                if ("triggerMode".equals(key)) {
                    queryChain.where(TEST_PLAN_REPORT.TRIGGER_MODE.in(filter.get(key)));
                }
                if ("execResult".equals(key)) {
                    queryChain.where(TEST_PLAN_REPORT.RESULT_STATUS.in(filter.get(key)));
                }
            }
        }
        return queryChain.pageAs(Page.of(request.getCurrent(), request.getPageSize()), TestPlanExecuteHisDTO.class);
    }

    @Override
    public void deleteByProjectId(String projectId) {
        List<String> testPlanIdList = queryChain().where(TEST_PLAN.PROJECT_ID.eq(projectId)).list().stream().map(TestPlan::getId).toList();
        //删除测试计划模块
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanModuleMapper.deleteByQuery(QueryChain.of(testPlanModuleMapper).where(TEST_PLAN_MODULE.PROJECT_ID.eq(projectId))));
        SubListUtils.dealForSubList(testPlanIdList, SubListUtils.DEFAULT_BATCH_SIZE, dealList -> {
            this.deleteByList(testPlanIdList);
        });
    }

    private void handleTags(TestPlanBatchEditRequest request, String userId, List<String> ids) {
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            if (request.isAppend()) {
                //追加标签
                List<TestPlan> planList = mapper.selectListByIds(ids);
                Map<String, TestPlan> collect = planList.stream().collect(Collectors.toMap(TestPlan::getId, v -> v));
                ids.forEach(id -> {
                    TestPlan testPlan = new TestPlan();
                    if (CollectionUtils.isNotEmpty(collect.get(id).getTags())) {
                        List<String> tags = collect.get(id).getTags();
                        tags.addAll(request.getTags());
                        testPlan.setTags(ServiceUtils.parseTags(tags));
                    } else {
                        testPlan.setTags(ServiceUtils.parseTags(request.getTags()));
                    }
                    testPlan.setId(id);
                    testPlan.setUpdateUser(userId);
                    mapper.update(testPlan);
                });
            } else {
                //替换标签
                updateChain().set(TestPlan::getTags, ServiceUtils.parseTags(request.getTags()))
                        .set(TEST_PLAN.PROJECT_ID, request.getProjectId())
                        .set(TEST_PLAN.UPDATE_USER, userId).where(TEST_PLAN.ID.in(ids)).update();
            }
        }
    }

    private void initResourceDefaultCollection(String planId, List<TestPlanCollection> allCollections) {
        List<TestPlanCollection> defaultCollections = new ArrayList<>();
        allCollections.forEach(allCollection -> defaultCollections.addAll(allCollection.getChildren()));
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> v.initResourceDefaultCollection(planId, defaultCollections));
    }

    private Boolean checkIsFollowCase(String id, String userId) {
        return QueryChain.of(TestPlanFollower.class).where(TEST_PLAN_FOLLOWER.TEST_PLAN_ID.eq(id)
                .and(TEST_PLAN_FOLLOWER.USER_ID.eq(userId))).exists();
    }

    private void getOtherConfig(TestPlanDetailResponse response, TestPlan testPlan) {
        QueryChain<TestPlanConfig> queryChain = QueryChain.of(TestPlanConfig.class).where(TestPlanConfig::getTestPlanId).eq(testPlan.getId());
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectOneByQuery(queryChain);
        response.setAutomaticStatusUpdate(testPlanConfig.getAutomaticStatusUpdate());
        response.setRepeatCase(testPlanConfig.getRepeatCase());
        response.setPassThreshold(testPlanConfig.getPassThreshold());
    }

    private void getGroupName(TestPlanDetailResponse response, TestPlan testPlan) {
        if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = mapper.selectOneById(testPlan.getGroupId());
            response.setGroupId(testPlan.getGroupId());
            response.setGroupName(group.getName());
        }
    }

    private void updateCompletedGroupStatus(String id, String userId) {
        List<TestPlan> testPlanList = queryChain().where(TEST_PLAN.GROUP_ID.eq(id)).list();
        if (CollectionUtils.isEmpty(testPlanList)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        List<String> ids = testPlanList.stream().map(TestPlan::getId).collect(Collectors.toList());
        ids.add(id);
        updateChain().set(TEST_PLAN.STATUS, TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)
                .set(TEST_PLAN.UPDATE_USER, userId)
                .where(TEST_PLAN.ID.in(ids)).update();
    }

    private boolean isTestPlanCompleted(String testPlanId) {
        TestPlanStatisticsResponse statisticsResponse = testPlanStatisticsService.calculateRate(Collections.singletonList(testPlanId)).getFirst();
        return StringUtils.equalsIgnoreCase(statisticsResponse.getStatus(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED);
    }

    private void cascadeDeleteTestPlanIds(List<String> testPlanIds, TestPlanReportService testPlanReportService) {
        Map<String, TestPlanResourceService> subTypes = CommonBeanFactory.getBeansOfType(TestPlanResourceService.class);
        subTypes.forEach((k, t) -> {
            t.deleteBatchByTestPlanId(testPlanIds);
        });
        //删除测试计划配置
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanConfigMapper.deleteByQuery(QueryChain.of(TestPlanConfig.class).where(TestPlanConfig::getTestPlanId).in(testPlanIds))
        );
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanFollowerMapper.deleteByQuery(QueryChain.of(TestPlanFollower.class).where(TestPlanFollower::getTestPlanId).in(testPlanIds))
        );
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanCaseExecuteHistoryMapper.deleteByQuery(
                        QueryChain.of(TestPlanCaseExecuteHistory.class).where(TestPlanCaseExecuteHistory::getTestPlanId).in(testPlanIds))
        );
        //删除测试计划报告
        testPlanReportService.deleteByTestPlanIds(testPlanIds);
        //删除定时任务
        scheduleService.deleteByResourceIds(testPlanIds, TestPlanScheduleJob.class.getName());
    }

    private TestPlan savePlanDTO(TestPlanCreateRequest createOrCopyRequest, String operator) {
        checkModule(createOrCopyRequest.getModuleId());
        if (StringUtils.equalsIgnoreCase(createOrCopyRequest.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                && !StringUtils.equalsIgnoreCase(createOrCopyRequest.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }
        TestPlan createTestPlan = new TestPlan();
        BeanUtils.copyProperties(createOrCopyRequest, createTestPlan);
        createTestPlan.setTags(ServiceUtils.parseTags(createTestPlan.getTags()));
        if (!StringUtils.equals(createTestPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = testPlanGroupService.validateGroupCapacity(createTestPlan.getGroupId(), 1);
            // 判断测试计划组是否存在
            createTestPlan.setModuleId(groupPlan.getModuleId());
        }
        initTestPlanPos(createTestPlan);
        createTestPlan.setNum(NumGenerator.nextNum(createOrCopyRequest.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        createTestPlan.setPlannedStartTime(DateUtils.getLocalDateTime(createOrCopyRequest.getPlannedStartTime()));
        createTestPlan.setPlannedEndTime(DateUtils.getLocalDateTime(createOrCopyRequest.getPlannedEndTime()));
        mapper.insert(createTestPlan);
        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(createOrCopyRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(createOrCopyRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(createOrCopyRequest.getPassThreshold());
        testPlanConfig.setCaseRunMode(ApiBatchRunMode.PARALLEL.name());
        testPlanConfigMapper.insert(testPlanConfig);
        return createTestPlan;
    }

    private void initTestPlanPos(TestPlan createTestPlan) {
        if (StringUtils.equals(createTestPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            createTestPlan.setPos(this.getNextOrder(createTestPlan.getProjectId(), createTestPlan.getGroupId()));
        } else {
            createTestPlan.setPos(testPlanGroupService.getNextOrder(createTestPlan.getGroupId()));
        }

    }

    private Long getNextOrder(String projectId, String groupId) {
        TestPlan one = queryChain().where(TEST_PLAN.PROJECT_ID.eq(projectId)
                .and(TEST_PLAN.GROUP_ID.eq(groupId))).one();
        long maxPos = 0;
        if (one != null) {
            maxPos = one.getPos();
        }
        return maxPos + ServiceUtils.POS_STEP;
    }

    private void deleteGroupByList(List<String> testPlanGroupList) {
        // todo
    }

    private void deleteByList(List<String> testPlanIds) {
        // todo
    }
}
