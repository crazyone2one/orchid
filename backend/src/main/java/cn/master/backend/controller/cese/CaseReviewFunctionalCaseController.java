package cn.master.backend.controller.cese;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.CaseReviewFunctionalCaseUser;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.functional.ReviewFunctionalCaseDTO;
import cn.master.backend.payload.dto.functional.ReviewerAndStatusDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.service.CaseReviewFunctionalCaseService;
import cn.master.backend.service.log.CaseReviewLogService;
import cn.master.backend.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Tag(name = "用例管理-用例评审-评审列表-评审详情")
@RestController
@RequestMapping("/case/review/detail")
@RequiredArgsConstructor
public class CaseReviewFunctionalCaseController {
    private final CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;

    @GetMapping("/get-ids/{reviewId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-获取已关联用例id集合(关联用例弹窗前调用)")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_RELEVANCE)
    //@CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public List<String> getCaseIds(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getCaseIdsByReviewId(reviewId);
    }

    @PostMapping("/page")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例列表")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public Page<ReviewFunctionalCaseDTO> page(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        String viewStatusUserId = StringUtils.EMPTY;
        if (request.isViewStatusFlag()) {
            viewStatusUserId = SessionUtils.getCurrentUserId();
        }
        return caseReviewFunctionalCaseService.page(request, false, viewStatusUserId);
    }

    @GetMapping("/tree/{reviewId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例列表模块树")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public List<BaseTreeNode> getTree(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getTree(reviewId);
    }

    @PostMapping("/module/count")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例统计模块数量")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        return caseReviewFunctionalCaseService.moduleCount(request, false);
    }

    @PostMapping("/batch/disassociate")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量取消关联用例")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_RELEVANCE)
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#logClass.batchDisassociateCaseLog(#request)", logClass = CaseReviewLogService.class)
    public void batchDisassociate(@Validated @RequestBody BaseReviewCaseBatchRequest request) {
        caseReviewFunctionalCaseService.disassociate(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-拖拽排序")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editPos(@Validated @RequestBody CaseReviewFunctionalCasePosRequest request) {
        caseReviewFunctionalCaseService.editPos(request);
    }

    @PostMapping("/batch/review")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量评审")
    @HasAnyAuthorize(value = {PermissionConstants.CASE_REVIEW_REVIEW, PermissionConstants.CASE_REVIEW_READ_UPDATE}, logical = Logical.OR)
    public void batchReview(@Validated @RequestBody BatchReviewFunctionalCaseRequest request) {
        caseReviewFunctionalCaseService.batchReview(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/batch/edit/reviewers")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量修改评审人")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void batchEditReviewUser(@Validated @RequestBody BatchEditReviewerRequest request) {
        caseReviewFunctionalCaseService.batchEditReviewUser(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/reviewer/status/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-评审结果的气泡数据")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public List<OptionDTO> getUserStatus(@Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable("reviewId") String reviewId, @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable("caseId") String caseId) {
        return caseReviewFunctionalCaseService.getUserStatus(reviewId, caseId);
    }

    @GetMapping("/reviewer/list/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-获取单个用例的评审人")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public List<CaseReviewFunctionalCaseUser> getReviewerList(@Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
                                                              @PathVariable("reviewId") String reviewId, @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
                                                              @PathVariable("caseId") String caseId) {
        return caseReviewFunctionalCaseService.getReviewerList(reviewId, caseId);
    }

    @GetMapping("/reviewer/status/total/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-评审总结过结果和每个评审人最后结果气泡数据")
    @HasAuthorize(PermissionConstants.CASE_REVIEW_READ)
    public ReviewerAndStatusDTO getUserAndStatus(@Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
                                                 @PathVariable("reviewId") String reviewId, @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
                                                 @PathVariable("caseId") String caseId) {
        return caseReviewFunctionalCaseService.getUserAndStatus(reviewId, caseId);
    }
}
