package cn.master.backend.controller.cese;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.CaseReview;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.functional.CaseReviewDTO;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.service.CaseReviewService;
import cn.master.backend.service.log.CaseReviewLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@Tag(name = "用例管理-用例评审")
@RestController
@RequiredArgsConstructor
@RequestMapping("/case/review")
public class CaseReviewController {
    private final CaseReviewService caseReviewService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-用例评审-用例列表查询")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public Page<CaseReviewDTO> getFunctionalCasePage(@Validated @RequestBody CaseReviewPageRequest request) {
        return caseReviewService.getCaseReviewPage(request);
    }

    @PostMapping("/module/count")
    @Operation(summary = "用例管理-用例评审-统计模块数量")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody CaseReviewPageRequest request) {
        return caseReviewService.moduleCount(request);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-创建用例评审")
    @Log(type = OperationLogType.ADD, expression = "#logClass.addCaseReviewLog(#request)", logClass = CaseReviewLogService.class)
    //@SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_ADD)
    public CaseReview addCaseReview(@Validated @RequestBody CaseReviewRequest request) {
        return caseReviewService.addCaseReview(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/copy")
    @Operation(summary = "用例管理-用例评审-复制用例评审")
    @Log(type = OperationLogType.ADD, expression = "#logClass.copyCaseReviewLog(#request)", logClass = CaseReviewLogService.class)
    //@SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_ADD)
    public CaseReview copyCaseReview(@Validated @RequestBody CaseReviewCopyRequest request) {
        return caseReviewService.copyCaseReview(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/edit")
    @Operation(summary = "用例管理-用例评审-编辑用例评审")
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateCaseReviewLog(#request)", logClass = CaseReviewLogService.class)
    //@SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editCaseReview(@Validated({Updated.class}) @RequestBody CaseReviewRequest request) {
        caseReviewService.editCaseReview(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/user-option/{projectId}")
    @Operation(summary = "用例管理-用例评审-获取具有评审权限的用户")
    @HasAnyAuthorize(value = {PermissionConstants.CASE_REVIEW_READ, PermissionConstants.CASE_REVIEW_READ_ADD, PermissionConstants.CASE_REVIEW_READ_UPDATE}, logical = Logical.OR)
    public List<UserDTO> getReviewUserList(@PathVariable String projectId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return caseReviewService.getReviewUserList(projectId, keyword);
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-用例评审-关注/取消关注用例")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editFollower(@Validated @RequestBody CaseReviewFollowerRequest request) {
        caseReviewService.editFollower(request.getCaseReviewId(), SessionUtils.getCurrentUserId());
    }

    @PostMapping("/associate")
    @Operation(summary = "用例管理-用例评审-关联用例")
    @Log(type = OperationLogType.ASSOCIATE, expression = "#logClass.associateCaseLog(#request)", logClass = CaseReviewLogService.class)
    @HasAuthorize(PermissionConstants.CASE_REVIEW_RELEVANCE)
    public void associateCase(@Validated @RequestBody CaseReviewAssociateRequest request) {
        caseReviewService.associateCase(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/disassociate/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-取消关联用例")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#logClass.disAssociateCaseLog(#reviewId, #caseId)", logClass = CaseReviewLogService.class)
    @HasAuthorize(PermissionConstants.CASE_REVIEW_RELEVANCE)
    public void disassociate(@PathVariable String reviewId, @PathVariable String caseId) {
        caseReviewService.disassociate(reviewId, caseId, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "用例管理-用例评审-拖拽排序")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editPos(@Validated @RequestBody PosRequest request) {
        caseReviewService.editPos(request);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "用例管理-用例评审-查看评审详情")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public CaseReviewDTO getCaseReviewDetail(@PathVariable String id) {
        return caseReviewService.getCaseReviewDetail(id, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/batch/move")
    @Operation(summary = "用例管理-用例评审-批量移动用例评审")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void batchMoveCaseReview(@Validated @RequestBody CaseReviewBatchRequest request) {
        caseReviewService.batchMoveCaseReview(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/delete/{projectId}/{reviewId}")
    @Operation(summary = "用例管理-用例评审-删除用例评审")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteFunctionalCaseLog(#reviewId)", logClass = CaseReviewLogService.class)
    public void deleteCaseReview(@PathVariable String reviewId, @PathVariable String projectId) {
        caseReviewService.deleteCaseReview(reviewId, projectId);
    }
}
