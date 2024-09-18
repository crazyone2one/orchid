package cn.master.backend.service;

import cn.master.backend.entity.*;
import cn.master.backend.handler.provider.CaseReviewCaseProvider;
import cn.master.backend.mapper.*;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.CaseReviewFunctionalCaseTableDef.CASE_REVIEW_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.CaseReviewUserTableDef.CASE_REVIEW_USER;
import static cn.master.backend.entity.table.CaseReviewTableDef.CASE_REVIEW;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Service
@RequiredArgsConstructor
public class DeleteCaseReviewService {
    private final CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    private final CaseReviewUserMapper caseReviewUserMapper;
    private final CaseReviewFunctionalCaseArchiveMapper caseReviewFunctionalCaseArchiveMapper;
    private final CaseReviewHistoryMapper caseReviewHistoryMapper;
    private final CaseReviewFollowerMapper caseReviewFollowerMapper;
    private final CaseReviewMapper caseReviewMapper;
    private final CaseReviewCaseProvider caseReviewCaseProvider;
    public void deleteCaseReviewResource(List<String> ids, String projectId) {
        // 1.刪除评审与功能用例关联关系
        QueryChain<CaseReviewFunctionalCase> caseReviewFunctionalCaseQueryChain = QueryChain.of(CaseReviewFunctionalCase.class)
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.in(ids));
        List<CaseReviewFunctionalCase> reviewFunctionalCases = caseReviewFunctionalCaseQueryChain.list();
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFunctionalCaseMapper.deleteByQuery(caseReviewFunctionalCaseQueryChain));
        // 2. 删除评审和评审人
        QueryChain<CaseReviewUser> caseReviewUserQueryChain = QueryChain.of(CaseReviewUser.class).where(CASE_REVIEW_USER.REVIEW_ID.in(ids));
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewUserMapper.deleteByQuery(caseReviewUserQueryChain));
        // 3. 删除归档的用例;
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFunctionalCaseArchiveMapper.deleteByQuery(
                QueryChain.of(CaseReviewFunctionalCaseArchive.class).where(CaseReviewFunctionalCaseArchive::getReviewId).in(ids)
        ));
        // 4.删除关注人
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFollowerMapper.deleteByQuery(
                QueryChain.of(CaseReviewFollower.class).where(CaseReviewFollower::getReviewId).in(ids)
        ));
        // 5.删除评审历史
        caseReviewHistoryMapper.deleteByQuery(
                QueryChain.of(CaseReviewHistory.class).where(CaseReviewHistory::getReviewId).in(ids)
        );
        //删除评审
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewMapper.deleteByQuery(
                QueryChain.of(CaseReview.class).where(CASE_REVIEW.ID.in(ids).and(CASE_REVIEW.PROJECT_ID.eq(projectId)))
        ));
        // 7. 批量刷新评审中其他用例的评审状态
        Map<String, List<CaseReviewFunctionalCase>> reviewFunctionalCaseMap = reviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
        reviewFunctionalCaseMap.forEach((reviewId, reviewFunctionalCaseList) -> caseReviewCaseProvider.refreshReviewCaseStatus(reviewFunctionalCaseList));

    }
}
