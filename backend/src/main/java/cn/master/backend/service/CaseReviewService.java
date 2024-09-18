package cn.master.backend.service;

import cn.master.backend.payload.dto.functional.CaseReviewDTO;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.payload.request.system.PosRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.CaseReview;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * 用例评审 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
public interface CaseReviewService extends IService<CaseReview> {

    CaseReview checkCaseReview(String caseReviewId);

    Long getCaseFunctionalCaseNextPos(String reviewId);

    Page<CaseReviewDTO> getCaseReviewPage(CaseReviewPageRequest request);

    Map<String, Long> moduleCount(CaseReviewPageRequest request);

    CaseReview addCaseReview(CaseReviewRequest request, String currentUserId);

    CaseReview copyCaseReview(CaseReviewCopyRequest request, String userId);

    void editCaseReview(CaseReviewRequest request, String userId);

    List<UserDTO> getReviewUserList(String projectId, String keyword);

    void editFollower(String caseReviewId, String userId);

    void associateCase(CaseReviewAssociateRequest request, String userId);

    void disassociate(String reviewId, String caseId, String currentUserId);

    void editPos(PosRequest request);

    CaseReviewDTO getCaseReviewDetail(String id, String userId);

    void batchMoveCaseReview(CaseReviewBatchRequest request, String userId);

    void deleteCaseReview(String reviewId, String projectId);
}
