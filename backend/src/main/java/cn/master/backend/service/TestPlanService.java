package cn.master.backend.service;

import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanCollection;
import cn.master.backend.payload.dto.plan.TestPlanExecuteHisDTO;
import cn.master.backend.payload.dto.system.LogInsertModule;
import cn.master.backend.payload.request.plan.*;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.payload.response.plan.TestPlanDetailResponse;
import cn.master.backend.payload.response.plan.TestPlanOperationResponse;
import com.mybatisflex.core.paginate.Page;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 测试计划 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
public interface TestPlanService extends BaseTestPlanService {

    void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod);

    TestPlan add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod);

    List<TestPlanCollection> initDefaultPlanCollection(String planId, String currentUser);

    TestPlan update(TestPlanUpdateRequest request, String userId, String requestUrl, String requestMethod);

    void checkTestPlanNotArchived(String testPlanId);

    void deleteScheduleConfig(String testPlanId);

    List<TestPlan> selectNotArchivedChildren(String testPlanGroupId);

    void delete(@NotBlank String id, String operator, String requestUrl, String requestMethod);

    void editFollower(String testPlanId, String userId);

    void archived(@NotBlank String testPlanId, String userId);

    TestPlanDetailResponse detail(@NotBlank String id, String userId);

    String copy(String planId, String userId);

    void filterArchivedIds(TestPlanBatchRequest request);

    long batchCopy(TestPlanBatchRequest request, String userId, String url, String method);

    long batchMove(TestPlanBatchRequest request, String userId, String operationUrl, String method);

    void batchArchived(TestPlanBatchProcessRequest request, String operator);

    boolean archived(TestPlan testPlan, String userId);

    void filterArchivedIds(TestPlanBatchEditRequest request);

    void batchEdit(TestPlanBatchEditRequest request, String currentUserId);

    TestPlanOperationResponse sort(PosRequest request, LogInsertModule logInsertModule);

    Page<TestPlanExecuteHisDTO> listHis(TestPlanExecuteHisPageRequest request);

    void deleteByProjectId(String projectId);
}
