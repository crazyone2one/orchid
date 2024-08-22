package cn.master.backend.controller.plan;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.constants.TestPlanResourceConfig;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.request.plan.TestPlanModuleCreateRequest;
import cn.master.backend.payload.request.plan.TestPlanModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.TestPlanManagementService;
import cn.master.backend.service.TestPlanModuleService;
import cn.master.backend.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Tag(name = "测试计划管理-模块树")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test-plan/module")
public class TestPlanModuleController {
    private final TestPlanModuleService testPlanModuleService;
    private final TestPlanManagementService testPlanManagementService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "测试计划管理-模块树-查找模块")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ)
    //@CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        testPlanManagementService.checkModuleIsOpen(projectId, TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN));
        return testPlanModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "测试计划管理-模块树-添加模块")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_ADD)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@RequestBody @Validated TestPlanModuleCreateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN));
        return testPlanModuleService.add(request, SessionUtils.getCurrentUserId(), "/test-plan/module/add", HttpMethodConstants.POST.name());
    }

    @PostMapping("/update")
    @Operation(summary = "测试计划管理-模块树-修改模块")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    //@CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan_module")
    public void list(@RequestBody @Validated TestPlanModuleUpdateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE, Collections.singletonList(TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN));
        testPlanModuleService.update(request, SessionUtils.getCurrentUserId(), "/test-plan/module/update", HttpMethodConstants.POST.name());
    }
    @GetMapping("/delete/{deleteId}")
    @Operation(summary = "测试计划管理-模块树-删除模块")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_DELETE)
    //@CheckOwner(resourceId = "#deleteId", resourceType = "test_plan_module")
    public void deleteNode(@PathVariable String deleteId) {
        testPlanManagementService.checkModuleIsOpen(deleteId, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE, Collections.singletonList(TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN));
        testPlanModuleService.deleteModule(deleteId, SessionUtils.getCurrentUserId(),"/test-plan/module/delete",HttpMethodConstants.GET.name());
    }
    @PostMapping("/move")
    @Operation(summary = "测试计划管理-模块树-移动模块")
    @HasAuthorize(PermissionConstants.TEST_PLAN_READ_UPDATE)
    //@CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "test_plan_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getDragNodeId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE, Collections.singletonList(TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN));
        testPlanModuleService.moveNode(request, SessionUtils.getCurrentUserId(),"/test-plan/module/move",HttpMethodConstants.POST.name());
    }
}
