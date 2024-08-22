package cn.master.backend.controller.plan;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.constants.TestPlanResourceConfig;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.plan.TestPlanExecuteHisDTO;
import cn.master.backend.payload.dto.system.LogInsertModule;
import cn.master.backend.payload.request.plan.*;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.payload.response.plan.TestPlanDetailResponse;
import cn.master.backend.payload.response.plan.TestPlanOperationResponse;
import cn.master.backend.payload.response.plan.TestPlanResponse;
import cn.master.backend.payload.response.plan.TestPlanSingleOperationResponse;
import cn.master.backend.service.TestPlanManagementService;
import cn.master.backend.service.TestPlanService;
import cn.master.backend.service.log.TestPlanLogService;
import cn.master.backend.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试计划 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@RestController
@Tag(name = "测试计划接口")
@RequiredArgsConstructor
@RequestMapping("/test-plan")
public class TestPlanController {

    private final TestPlanService testPlanService;
    private final TestPlanManagementService testPlanManagementService;

    @PostMapping("/add")
    @Operation(summary = "测试计划-创建测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    //@SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.sendAddNotice(#request)", targetClass = TestPlanSendNoticeService.class)
    public TestPlan add(@Validated @RequestBody TestPlanCreateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.add(request, SessionUtils.getCurrentUserId(), "/test-plan/add", HttpMethodConstants.POST.name());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "测试计划-删除测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_DELETE)
    public void remove(@NotBlank @PathVariable @Parameter(description = "测试计划主键") String id) {
        testPlanManagementService.checkModuleIsOpen(id, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.delete(id, SessionUtils.getCurrentUserId(), "/test-plan/delete", HttpMethodConstants.GET.name());
    }

    @PostMapping("/update")
    @Operation(summary = "测试计划-更新测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public TestPlan update(@Validated @RequestBody TestPlanUpdateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.update(request, SessionUtils.getCurrentUserId(), "/test-plan/update", HttpMethodConstants.POST.name());
    }


    @PostMapping("/page")
    @Operation(summary = "测试计划-表格分页查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public Page<TestPlanResponse> page(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.page(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "测试计划-抽屉详情(单个测试计划获取详情用于编辑)")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public TestPlanDetailResponse detail(@NotBlank @PathVariable String id) {
        return testPlanService.detail(id, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/group-list/{projectId}")
    @Operation(summary = "测试计划-测试计划组查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public List<TestPlan> groupList(@PathVariable String projectId) {
        testPlanManagementService.checkModuleIsOpen(projectId, TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.groupList(projectId);
    }

    @GetMapping("/list-in-group/{groupId}")
    @Operation(summary = "测试计划-表格分页查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    //@CheckOwner(resourceId = "#groupId", resourceType = "test_plan")
    public List<TestPlanResponse> listInGroup(@NotBlank @PathVariable String groupId) {
        testPlanManagementService.checkModuleIsOpen(groupId, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.selectByGroupId(groupId);
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-模块统计")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.moduleCount(request);
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "测试计划-关注/取消关注")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public void editFollower(@Validated @RequestBody TestPlanFollowerRequest request) {
        testPlanService.editFollower(request.getTestPlanId(), SessionUtils.getCurrentUserId());
    }

    @GetMapping("/archived/{id}")
    @Operation(summary = "测试计划-归档")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @Log(type = OperationLogType.ARCHIVED, expression = "#logClass.archivedLog(#id)", logClass = TestPlanLogService.class)
    public void archived(@NotBlank @PathVariable String id) {
        testPlanService.archived(id, SessionUtils.getCurrentUserId());
    }

    @PostMapping(value = "/batch-delete")
    @Operation(summary = "测试计划-批量删除")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_DELETE)
    public void delete(@Validated @RequestBody TestPlanBatchProcessRequest request) throws Exception {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchDelete(request, SessionUtils.getCurrentUserId(), "/test-plan/batch-delete", HttpMethodConstants.POST.name());
    }

    @GetMapping("/copy/{id}")
    @Operation(summary = "测试计划-复制测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    public TestPlanSingleOperationResponse copy(@PathVariable String id) {
        return new TestPlanSingleOperationResponse(testPlanService.copy(id, SessionUtils.getCurrentUserId()));
    }

    @PostMapping("/batch-copy")
    @Operation(summary = "测试计划-批量复制测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    public TestPlanOperationResponse batchCopy(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        return new TestPlanOperationResponse(
                testPlanService.batchCopy(request, SessionUtils.getCurrentUserId(), "/test-plan/batch-copy", HttpMethodConstants.POST.name())
        );
    }

    @PostMapping("/batch-move")
    @Operation(summary = "测试计划-批量移动测试计划")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public TestPlanOperationResponse batchMove(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        return new TestPlanOperationResponse(
                testPlanService.batchMove(request, SessionUtils.getCurrentUserId(), "/test-plan/batch-move", HttpMethodConstants.POST.name())
        );
    }

    @PostMapping("/batch-archived")
    @Operation(summary = "测试计划-批量归档")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public void batchArchived(@Validated @RequestBody TestPlanBatchProcessRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchArchived(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/batch-edit")
    @Operation(summary = "测试计划-批量编辑")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.batchEditLog(#request)", logClass = TestPlanLogService.class)
    public void batchEdit(@Validated @RequestBody TestPlanBatchEditRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        testPlanService.batchEdit(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划移动（测试计划拖进、拖出到测试计划组、测试计划在测试计划组内的排序")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    public TestPlanOperationResponse sortTestPlan(@Validated @RequestBody PosRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getMoveId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.sort(request, new LogInsertModule(SessionUtils.getCurrentUserId(), "/test-plan/sort", HttpMethodConstants.POST.name()));
    }

    @PostMapping(value = "/his/page")
    @Operation(summary = "测试计划-执行历史-列表分页查询")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    //@CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Page<TestPlanExecuteHisDTO> pageHis(@Validated @RequestBody TestPlanExecuteHisPageRequest request) {
        return testPlanService.listHis(request);
    }
}
