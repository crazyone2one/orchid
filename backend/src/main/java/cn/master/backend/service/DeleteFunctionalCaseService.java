package cn.master.backend.service;

import cn.master.backend.constants.DefaultRepositoryDir;
import cn.master.backend.entity.*;
import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.FileRequest;
import cn.master.backend.mapper.*;
import cn.master.backend.util.LogUtils;
import cn.master.backend.util.RelationshipEdgeUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Service
@RequiredArgsConstructor
public class DeleteFunctionalCaseService {
    private final FunctionalCaseTestMapper functionalCaseTestMapper;
    private final FunctionalCaseDemandMapper functionalCaseDemandMapper;
    private final FunctionalCaseRelationshipEdgeMapper functionalCaseRelationshipEdgeMapper;
    private final CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    private final FunctionalCaseCommentMapper functionalCaseCommentMapper;
    private final FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;
    private final FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    private final FunctionalCaseMapper functionalCaseMapper;
    private final CaseReviewHistoryMapper caseReviewHistoryMapper;
    private final TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    private final TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deleteFunctionalCaseResource(List<String> ids, String projectId) {
        //1.刪除用例与其他用例关联关系
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseTestMapper.deleteByQuery(QueryChain.of(functionalCaseTestMapper).where(FunctionalCaseTest::getCaseId).in(ids))
        );
        // 删除关联需求
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseDemandMapper.deleteByQuery(QueryChain.of(functionalCaseDemandMapper).where(FunctionalCaseDemand::getCaseId).in(ids))
        );
        // 删除依赖关系
        List<FunctionalCaseRelationshipEdge> edgeList = QueryChain.of(functionalCaseRelationshipEdgeMapper)
                .where(FunctionalCaseRelationshipEdge::getSourceId).in(ids).or(FunctionalCaseRelationshipEdge::getTargetId).in(ids).list();
        if (CollectionUtils.isNotEmpty(edgeList)) {
            List<String> edgeIds = edgeList.stream().map(FunctionalCaseRelationshipEdge::getId).toList();
            edgeIds.forEach(RelationshipEdgeUtils::updateGraphId);
        }
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseRelationshipEdgeMapper.deleteByQuery(QueryChain.of(functionalCaseRelationshipEdgeMapper)
                        .where(FunctionalCaseRelationshipEdge::getId).in(ids))
        );
        // 删除关联评审
        LogicDeleteManager.execWithoutLogicDelete(() ->
                caseReviewFunctionalCaseMapper.deleteByQuery(QueryChain.of(caseReviewFunctionalCaseMapper)
                        .where(CaseReviewFunctionalCase::getCaseId).in(ids))
        );
        // 评论
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseCommentMapper.deleteByQuery(QueryChain.of(functionalCaseCommentMapper)
                        .where(FunctionalCaseComment::getCaseId).in(ids))
        );
        // 附件
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseAttachmentMapper.deleteByQuery(QueryChain.of(functionalCaseAttachmentMapper)
                        .where(FunctionalCaseAttachment::getCaseId).in(ids))
        );
        //删除文件
        FileRequest request = new FileRequest();
        // 删除文件所在目录
        for (String id : ids) {
            request.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, id));
            try {
                FileCenter.getDefaultRepository().deleteFolder(request);
                request.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, id));
                FileCenter.getDefaultRepository().deleteFolder(request);
            } catch (Exception e) {
                LogUtils.error("彻底删除功能用例，文件删除失败：{}", e);
            }
        }
        // 自定义字段
        LogicDeleteManager.execWithoutLogicDelete(() ->
                functionalCaseCustomFieldMapper.deleteByQuery(QueryChain.of(functionalCaseCustomFieldMapper)
                        .where(FunctionalCaseCustomField::getCaseId).in(ids))
        );
        functionalCaseMapper.deleteByQuery(QueryChain.of(functionalCaseMapper)
                .where(FunctionalCase::getId).in(ids).and(FunctionalCase::getProjectId).eq(projectId));
        //删除评审历史
        caseReviewHistoryMapper.deleteByQuery(QueryChain.of(caseReviewHistoryMapper).where(CaseReviewHistory::getCaseId).in(ids));
        //删除和测试计划的关联关系
        LogicDeleteManager.execWithoutLogicDelete(() ->
                testPlanFunctionalCaseMapper.deleteByQuery(QueryChain.of(testPlanFunctionalCaseMapper)
                        .where(TestPlanFunctionalCase::getFunctionalCaseId).in(ids))
        );
        // 删除执行历史
        testPlanCaseExecuteHistoryMapper.deleteByQuery(QueryChain.of(testPlanCaseExecuteHistoryMapper).where(TestPlanCaseExecuteHistory::getCaseId).in(ids));
    }
}
