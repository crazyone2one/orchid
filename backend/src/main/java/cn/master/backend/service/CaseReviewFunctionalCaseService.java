package cn.master.backend.service;

import cn.master.backend.entity.CaseReviewFunctionalCaseUser;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.functional.ReviewFunctionalCaseDTO;
import cn.master.backend.payload.dto.functional.ReviewerAndStatusDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.request.functional.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.CaseReviewFunctionalCase;

import java.util.List;
import java.util.Map;

/**
 * 用例评审和功能用例的中间表 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
public interface CaseReviewFunctionalCaseService extends IService<CaseReviewFunctionalCase> {

    void addCaseReviewFunctionalCase(String caseId, String userId, String reviewId);

    Long getCaseFunctionalCaseNextPos(String reviewId);

    void reReviewedCase(FunctionalCaseEditRequest request, FunctionalCase functionalCase, String userId);

    List<String> doSelectIds(BaseReviewCaseBatchRequest request);

    List<String> getCaseIdsByReviewId(String reviewId);

    Page<ReviewFunctionalCaseDTO> page(ReviewFunctionalCasePageRequest request, boolean deleted, String viewStatusUserId);

    List<BaseTreeNode> getTree(String reviewId);

    Map<String, Long> moduleCount(ReviewFunctionalCasePageRequest request, boolean b);

    void disassociate(BaseReviewCaseBatchRequest request, String userId);

    void editPos(CaseReviewFunctionalCasePosRequest request);

    void batchReview(BatchReviewFunctionalCaseRequest request, String userId);

    void batchEditReviewUser(BatchEditReviewerRequest request, String userId);

    List<OptionDTO> getUserStatus(String reviewId, String caseId);

    List<CaseReviewFunctionalCaseUser> getReviewerList(String reviewId, String caseId);

    ReviewerAndStatusDTO getUserAndStatus(String reviewId, String caseId);
}
