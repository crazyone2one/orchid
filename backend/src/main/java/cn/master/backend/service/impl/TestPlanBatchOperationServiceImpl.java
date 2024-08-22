package cn.master.backend.service.impl;

import cn.master.backend.constants.ApplicationNumScope;
import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.Schedule;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanCollection;
import cn.master.backend.entity.TestPlanConfig;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.job.TestPlanScheduleJob;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.TestPlanCollectionMapper;
import cn.master.backend.mapper.TestPlanConfigMapper;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.payload.request.system.BaseScheduleConfigRequest;
import cn.master.backend.payload.response.plan.TestPlanResponse;
import cn.master.backend.service.*;
import cn.master.backend.util.CommonBeanFactory;
import cn.master.backend.util.JSON;
import cn.master.backend.util.NumGenerator;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.master.backend.entity.table.TestPlanCollectionTableDef.TEST_PLAN_COLLECTION;
import static cn.master.backend.entity.table.TestPlanConfigTableDef.TEST_PLAN_CONFIG;
import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 08/21/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanBatchOperationServiceImpl extends BaseTestPlanServiceImpl implements TestPlanBatchOperationService {
    private final TestPlanMapper testPlanMapper;
    private final TestPlanConfigMapper testPlanConfigMapper;
    private final TestPlanGroupService testPlanGroupService;
    private final TestPlanCollectionMapper testPlanCollectionMapper;
    private final ApplicationContext applicationContext;
    private final ScheduleService scheduleService;
    private final TestPlanScheduleService testPlanScheduleService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPlan copyPlanGroup(TestPlan originalGroup, String targetId, String targetType, String operator) {
        //测试计划组复制的时候，只支持targetType为module的操作. 已归档的无法操作
        if (StringUtils.equalsIgnoreCase(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                || StringUtils.equalsIgnoreCase(originalGroup.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }
        super.checkModule(targetId);
        List<TestPlan> childList = QueryChain.of(TestPlan.class).where(TEST_PLAN.GROUP_ID.eq(originalGroup.getId())
                        .and(TEST_PLAN.STATUS.ne(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)))
                .orderBy(TEST_PLAN.POS.asc()).list();
        TestPlan testPlanGroup = new TestPlan();
        BeanUtils.copyProperties(originalGroup, testPlanGroup);
        testPlanGroup.setNum(NumGenerator.nextNum(testPlanGroup.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlanGroup.setName(getCopyName(originalGroup.getName(), originalGroup.getNum(), testPlanGroup.getNum()));
        testPlanGroup.setCreateUser(operator);
        testPlanGroup.setUpdateUser(operator);
        testPlanGroup.setModuleId(targetId);
        testPlanGroup.setPos(testPlanGroupService.getNextOrder(originalGroup.getGroupId()));
        testPlanGroup.setActualEndTime(null);
        testPlanGroup.setActualStartTime(null);
        testPlanGroup.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlanGroup);
        TestPlanConfig originalTestPlanConfig = QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.eq(originalGroup.getId())).one();
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyProperties(originalTestPlanConfig, newTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlanGroup.getId());
            testPlanConfigMapper.insertSelective(newTestPlanConfig);
        }
        for (TestPlan child : childList) {
            copyPlan(child, testPlanGroup.getId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, operator);
        }

        // 复制计划组-定时任务信息
        copySchedule(originalGroup.getId(), testPlanGroup.getId(), operator);
        return testPlanGroup;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TestPlan copyPlan(TestPlan originalTestPlan, String targetId, String targetType, String operator) {
        //已归档的无法操作
        if (StringUtils.equalsIgnoreCase(originalTestPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException(Translator.get("test_plan.error"));
        }
        String moduleId = originalTestPlan.getModuleId();
        String groupId = originalTestPlan.getGroupId();
        long pos = originalTestPlan.getPos();
        String sortRangeId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;
        if (StringUtils.equals(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            if (!StringUtils.equalsIgnoreCase(targetId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                TestPlan group = testPlanMapper.selectOneById(targetId);
                //如果目标ID是测试计划组， 需要进行容量校验
                if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
                    testPlanGroupService.validateGroupCapacity(targetId, 1);
                }
                moduleId = group.getModuleId();
                sortRangeId = targetId;
            }
            groupId = targetId;
        } else {
            super.checkModule(targetId);
            moduleId = targetId;
        }
        TestPlan testPlan = new TestPlan();
        BeanUtils.copyProperties(originalTestPlan, testPlan);
        testPlan.setNum(NumGenerator.nextNum(testPlan.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlan.setName(this.getCopyName(originalTestPlan.getName(), originalTestPlan.getNum(), testPlan.getNum()));
        testPlan.setCreateUser(operator);
        testPlan.setUpdateUser(operator);
        testPlan.setModuleId(moduleId);
        testPlan.setGroupId(groupId);
        testPlan.setPos(testPlanGroupService.getNextOrder(sortRangeId));
        testPlan.setActualEndTime(null);
        testPlan.setActualStartTime(null);
        testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlan);
        TestPlanConfig originalTestPlanConfig = QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.eq(originalTestPlan.getId())).one();
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyProperties(originalTestPlanConfig, newTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlan.getId());
            testPlanConfigMapper.insertSelective(newTestPlanConfig);
        }
        List<TestPlanCollection> testPlanCollectionList = QueryChain.of(TestPlanCollection.class)
                .where(TEST_PLAN_COLLECTION.TEST_PLAN_ID.eq(originalTestPlan.getId())
                        .and(TEST_PLAN_COLLECTION.PARENT_ID.eq(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))).list();
        Map<String, String> oldCollectionIdToNewCollectionId = new HashMap<>();
        if (testPlanCollectionList.isEmpty()) {
            Objects.requireNonNull(CommonBeanFactory.getBean(TestPlanService.class)).initDefaultPlanCollection(testPlan.getId(), operator);
        } else {
            List<TestPlanCollection> newTestPlanCollectionList = new ArrayList<>();
            for (TestPlanCollection testPlanCollection : testPlanCollectionList) {
                TestPlanCollection newTestPlanCollection = new TestPlanCollection();
                BeanUtils.copyProperties(testPlanCollection, newTestPlanCollection);
                newTestPlanCollection.setTestPlanId(testPlan.getId());
                newTestPlanCollection.setCreateUser(operator);
                newTestPlanCollectionList.add(newTestPlanCollection);

                //查找测试集信息
                List<TestPlanCollection> children = QueryChain.of(TestPlanCollection.class)
                        .where(TEST_PLAN_COLLECTION.PARENT_ID.eq(testPlanCollection.getId())).list();
                for (TestPlanCollection child : children) {
                    TestPlanCollection childCollection = new TestPlanCollection();
                    BeanUtils.copyProperties(child, childCollection);
                    childCollection.setId(IDGenerator.nextStr());
                    childCollection.setParentId(newTestPlanCollection.getId());
                    childCollection.setTestPlanId(testPlan.getId());
                    childCollection.setCreateUser(operator);
                    newTestPlanCollectionList.add(childCollection);
                    oldCollectionIdToNewCollectionId.put(child.getId(), childCollection.getId());
                }
            }
            testPlanCollectionMapper.insertBatch(newTestPlanCollectionList);
        }
        //测试用例信息
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> {
            v.copyResource(originalTestPlan.getId(), testPlan.getId(), oldCollectionIdToNewCollectionId, operator);
        });

        // 复制计划-定时任务信息
        copySchedule(originalTestPlan.getId(), testPlan.getId(), operator);

        return testPlan;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public List<TestPlan> batchCopy(List<TestPlan> originalPlanList, String targetId, String targetType, String userId) {
        List<TestPlan> copyPlanResult = new ArrayList<>();
        //如果目标ID是测试计划组， 需要进行容量校验
        if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
            testPlanGroupService.validateGroupCapacity(targetId, originalPlanList.size());
        }
        /*
            此处不进行批量处理，原因有两点：
            1） 测试计划内（或者测试计划组内）数据量不可控，选择批量操作时更容易出现数据太多不走索引、数据太多内存溢出等问题。不批量操作可以减少这些问题出现的概率，代价是速度会变慢。
            2） 作为数据量不可控的操作，如果数据量少，不采用批量处理也不会消耗太多时间。如果数据量多，就会容易出现1的问题。并且本人不建议针对不可控数据量的数据支持批量操作。
         */
        for (TestPlan copyPlan : originalPlanList) {
            if (StringUtils.equalsIgnoreCase(copyPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                copyPlanResult.add(copyPlanGroup(copyPlan, targetId, targetType, userId));
            } else {
                copyPlanResult.add(copyPlan(copyPlan, targetId, targetType, userId));
            }
        }
        return copyPlanResult;
    }

    @Override
    public long batchMoveGroup(List<TestPlan> testPlanList, String groupId, String userId) {
        // 判断测试计划组是否存在
        String groupModuleId = null;
        if (!StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = mapper.selectOneById(groupId);
            groupModuleId = groupPlan.getModuleId();
        }
        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.is.archived"));
            }
            // 已归档的测试计划无法操作 测试计划组无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                throw new MSException(Translator.get("test_plan.group.error"));
            }
            movePlanIds.add(testPlan.getId());
        }
        long nextPos = testPlanGroupService.getNextOrder(groupId);
        for (TestPlan testPlan : testPlanList) {
            TestPlan updatePlan = new TestPlan();
            updatePlan.setId(testPlan.getId());
            updatePlan.setGroupId(groupId);
            updatePlan.setModuleId(StringUtils.isBlank(groupModuleId) ? testPlan.getModuleId() : groupModuleId);
            updatePlan.setPos(nextPos);
            updatePlan.setUpdateUser(userId);
            mapper.update(updatePlan);
        }
        return testPlanList.size();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public long batchMoveModule(List<TestPlan> testPlanList, String moduleId, String userId) {
        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.is.archived"));
            }
            if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                // 测试计划组下的测试计划不单独处理， 如果勾选了他的测试计划组，会在下面进行逻辑补足。
                continue;
            }

            movePlanIds.add(testPlan.getId());
            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                List<TestPlanResponse> testPlanItemList = selectByGroupIds(Collections.singletonList(testPlan.getId()));
                for (TestPlanResponse item : testPlanItemList) {
                    movePlanIds.add(item.getId());
                }
            }
        }
        movePlanIds = movePlanIds.stream().distinct().toList();
        return batchMovePlan(movePlanIds, moduleId, userId);
    }

    private long batchMovePlan(List<String> ids, String moduleId, String userId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            updateChain().set(TEST_PLAN.MODULE_ID, moduleId)
                    .set(TEST_PLAN.UPDATE_USER, userId)
                    .where(TEST_PLAN.ID.in(ids)).update();
            return ids.size();
        } else {
            return 0;
        }
    }

    private List<TestPlanResponse> selectByGroupIds(List<String> strings) {
        return queryChain().select(TEST_PLAN.ALL_COLUMNS)
                .select(TEST_PLAN.GROUP_ID.as("parent")).from(TEST_PLAN)
                .where(TEST_PLAN.GROUP_ID.in(strings))
                .orderBy(TEST_PLAN.POS.desc()).listAs(TestPlanResponse.class);
    }

    private void copySchedule(String resourceId, String targetId, String operator) {
        Schedule originalSchedule = scheduleService.getScheduleByResource(resourceId, TestPlanScheduleJob.class.getName());
        if (originalSchedule != null) {
            // 来源的 "计划/计划组" 存在定时任务即复制, 无论开启或关闭
            BaseScheduleConfigRequest scheduleRequest = new BaseScheduleConfigRequest();
            scheduleRequest.setEnable(originalSchedule.getEnable());
            scheduleRequest.setCron(originalSchedule.getValue());
            // noinspection unchecked
            scheduleRequest.setRunConfig(JSON.parseMap(originalSchedule.getConfig()));
            scheduleRequest.setResourceId(targetId);
            testPlanScheduleService.scheduleConfig(scheduleRequest, operator);
        }
    }

    private String getCopyName(String name, long oldNum, long newNum) {
        if (!StringUtils.startsWith(name, "copy_")) {
            name = "copy_" + name;
        }
        if (name.length() > 250) {
            name = name.substring(0, 200) + "...";
        }
        if (StringUtils.endsWith(name, "_" + oldNum)) {
            name = StringUtils.substringBeforeLast(name, "_" + oldNum);
        }
        name = name + "_" + newNum;
        return name;
    }
}
