package cn.master.backend.controller.cese;

import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.request.functional.FunctionalCaseModuleCreateRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.impl.FunctionalCaseModuleServiceImpl;
import cn.master.backend.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Tag(name = "用例管理-功能用例-模块")
@RestController
@RequiredArgsConstructor
@RequestMapping("/functional/case/module")
public class FunctionCaseModuleController {
    private final FunctionalCaseModuleServiceImpl functionalCaseModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "用例管理-功能用例-模块-获取模块树")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return functionalCaseModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例-模块-添加模块")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    public String add(@RequestBody @Validated FunctionalCaseModuleCreateRequest request) {
        return functionalCaseModuleService.add(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "用例管理-功能用例-模块-修改模块")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void list(@RequestBody @Validated FunctionalCaseModuleUpdateRequest request) {
        functionalCaseModuleService.update(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "用例管理-功能用例-模块-移动模块")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        functionalCaseModuleService.moveNode(request, SessionUtils.getCurrentUserId());
    }
    @GetMapping("/delete/{moduleId}")
    @Operation(summary = "用例管理-功能用例-模块-删除模块")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    public void deleteNode(@PathVariable String moduleId) {
        functionalCaseModuleService.deleteModule(moduleId, SessionUtils.getCurrentUserId());
    }
}
