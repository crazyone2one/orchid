package cn.master.backend.service;

import cn.master.backend.entity.TestPlan;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @author Created by 11's papa on 08/21/2024
 **/
public interface TestPlanBatchOperationService extends BaseTestPlanService{
    TestPlan copyPlanGroup(TestPlan originalGroup, String targetId, String targetType, String operator);

    TestPlan copyPlan(TestPlan originalTestPlan, String targetId, String targetType, String operator);

    List<TestPlan> batchCopy(List<TestPlan> originalPlanList, String targetId, String targetType, String userId);

    long batchMoveGroup(List<TestPlan> testPlanList, String groupId, String userId);

    long batchMoveModule(List<TestPlan> testPlanList, String moduleId, String userId);
}
