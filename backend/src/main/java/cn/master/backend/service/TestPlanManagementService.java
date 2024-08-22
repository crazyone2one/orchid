package cn.master.backend.service;

import cn.master.backend.entity.TestPlan;
import cn.master.backend.payload.request.plan.TestPlanTableRequest;
import cn.master.backend.payload.response.plan.TestPlanResponse;
import com.mybatisflex.core.paginate.Page;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
public interface TestPlanManagementService {
    void checkModuleIsOpen(String resourceId, String resourceType, List<String> moduleMenus);

    Page<TestPlanResponse> page(TestPlanTableRequest request);

    List<TestPlan> groupList(String projectId);

    List<TestPlanResponse> selectByGroupId(@NotBlank String groupId);

    Map<String, Long> moduleCount(TestPlanTableRequest request);
}
