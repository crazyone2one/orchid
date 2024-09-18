package cn.master.backend.controller.cese;

import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.request.functional.CaseReviewModuleCreateRequest;
import cn.master.backend.payload.request.functional.CaseReviewModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.CaseReviewModuleService;
import cn.master.backend.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Tag(name = "用例管理-用例评审-模块")
@RestController
@RequiredArgsConstructor
@RequestMapping("/case/review/module")
public class CaseReviewModuleController {
    private final CaseReviewModuleService caseReviewModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "用例管理-用例评审-模块-获取模块树")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return caseReviewModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-模块-添加模块")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_ADD)
    public String add(@RequestBody @Validated CaseReviewModuleCreateRequest request) {
        return caseReviewModuleService.add(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "用例管理-用例评审-模块-修改模块")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void list(@RequestBody @Validated CaseReviewModuleUpdateRequest request) {
        caseReviewModuleService.update(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "用例管理-用例评审-模块-移动模块")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    //@CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "case_review_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        caseReviewModuleService.moveNode(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/delete/{moduleId}")
    @Operation(summary = "用例管理-用例评审-模块-删除模块")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_DELETE)
    //@CheckOwner(resourceId = "#moduleId", resourceType = "case_review_module")
    public void deleteNode(@PathVariable String moduleId) {
        caseReviewModuleService.deleteModule(moduleId);
    }
}
