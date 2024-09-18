package cn.master.backend.service.impl;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.CaseReview;
import cn.master.backend.entity.CaseReviewModule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.CaseReviewModuleMapper;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.project.NodeSortDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.functional.CaseReviewModuleCreateRequest;
import cn.master.backend.payload.request.functional.CaseReviewModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.CaseReviewModuleService;
import cn.master.backend.service.DeleteCaseReviewService;
import cn.master.backend.service.ModuleTreeService;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.master.backend.entity.table.CaseReviewModuleTableDef.CASE_REVIEW_MODULE;
import static cn.master.backend.entity.table.CaseReviewTableDef.CASE_REVIEW;

/**
 * 用例评审模块 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class CaseReviewModuleServiceImpl extends ModuleTreeService implements CaseReviewModuleService {
    private final CaseReviewModuleMapper caseReviewModuleMapper;
    private final OperationLogService operationLogService;
    private final DeleteCaseReviewService deleteCaseReviewService;

    @Override
    public Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = getTreeOnlyIdsAndResourceCount(projectId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return super.getIdCountMapByBreadth(treeNodeList);
    }

    @Override
    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = QueryChain.of(CaseReviewModule.class)
                .select(CASE_REVIEW_MODULE.ID, CASE_REVIEW_MODULE.NAME, CASE_REVIEW_MODULE.PARENT_ID)
                .select("'module' AS type")
                .from(CASE_REVIEW_MODULE).where(CASE_REVIEW_MODULE.PROJECT_ID.eq(projectId))
                .orderBy(CASE_REVIEW_MODULE.POS.desc())
                .listAs(BaseTreeNode.class);
        return super.buildTreeAndCountResource(fileModuleList, true, Translator.get("review.module.default.name"));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String add(CaseReviewModuleCreateRequest request, String userId) {
        CaseReviewModule caseReviewModule = new CaseReviewModule();
        caseReviewModule.setName(request.getName());
        caseReviewModule.setParentId(request.getParentId());
        caseReviewModule.setProjectId(request.getProjectId());
        checkDataValidity(caseReviewModule);
        caseReviewModule.setPos(countPos(request.getParentId()));
        caseReviewModule.setCreateUser(userId);
        caseReviewModule.setUpdateUser(userId);
        caseReviewModuleMapper.insert(caseReviewModule);
        return caseReviewModule.getId();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void update(CaseReviewModuleUpdateRequest request, String userId) {
        CaseReviewModule updateModule = caseReviewModuleMapper.selectOneById(request.getId());
        if (updateModule == null) {
            throw new MSException(Translator.get("case_module.not.exist"));
        }
        updateModule.setName(request.getName());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateUser(userId);
        caseReviewModuleMapper.update(updateModule);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void moveNode(NodeMoveRequest request, String userId) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                caseReviewModuleMapper::selectBaseModuleById,
                caseReviewModuleMapper::selectModuleByParentIdAndPosOperator);
        QueryChain<CaseReviewModule> queryChain = QueryChain.of(caseReviewModuleMapper)
                .where(CASE_REVIEW_MODULE.PARENT_ID.eq(nodeSortDTO.getParent().getId())
                        .and(CASE_REVIEW_MODULE.ID.eq(request.getDragNodeId())));
        if (!queryChain.exists()) {
            CaseReviewModule moveModule = caseReviewModuleMapper.selectOneById(request.getDragNodeId());
            moveModule.setParentId(nodeSortDTO.getParent().getId());
            this.checkDataValidity(moveModule);
            CaseReviewModule caseReviewModule = new CaseReviewModule();
            caseReviewModule.setId(request.getDragNodeId());
            caseReviewModule.setParentId(nodeSortDTO.getParent().getId());
            caseReviewModule.setUpdateUser(userId);
            caseReviewModuleMapper.update(caseReviewModule);
        }
        super.sort(nodeSortDTO);
    }

    @Override
    public void deleteModule(String moduleId) {
        CaseReviewModule deleteModule = caseReviewModuleMapper.selectOneById(moduleId);
        if (Objects.nonNull(deleteModule)) {
            List<CaseReview> caseReviews = deleteModuleByIds(Collections.singletonList(moduleId), new ArrayList<>(), deleteModule.getProjectId());
            batchDelLog(caseReviews, deleteModule.getProjectId());
        }
    }

    private void batchDelLog(List<CaseReview> caseReviews, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        caseReviews.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_REVIEW,
                    item.getName());

            dto.setPath("/case/review/module/delete/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }

    private List<CaseReview> deleteModuleByIds(List<String> deleteIds, List<CaseReview> caseReviews, String projectId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return caseReviews;
        }
        LogicDeleteManager.execWithoutLogicDelete(() ->
                caseReviewModuleMapper.deleteByQuery(QueryChain.of(caseReviewModuleMapper).where(CASE_REVIEW_MODULE.ID.in(deleteIds))));
        List<CaseReview> caseReviewList = checkCaseByModuleIds(deleteIds);
        if (CollectionUtils.isNotEmpty(caseReviewList)) {
            caseReviews.addAll(caseReviewList);
        }
        List<String> reviewIds = caseReviewList.stream().map(CaseReview::getId).toList();
        if (CollectionUtils.isNotEmpty(reviewIds)) {
            deleteCaseReviewService.deleteCaseReviewResource(reviewIds, projectId);
        }
        List<String> childrenIds = QueryChain.of(caseReviewModuleMapper)
                .select(CASE_REVIEW_MODULE.ID).from(CASE_REVIEW_MODULE)
                .where(CASE_REVIEW_MODULE.PARENT_ID.in(deleteIds)).listAs(String.class);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds, caseReviews, projectId);
        }
        return caseReviews;
    }

    private List<CaseReview> checkCaseByModuleIds(List<String> ids) {
        return QueryChain.of(CaseReview.class)
                .where(CASE_REVIEW.MODULE_ID.in(ids))
                .list();
    }

    private Long countPos(String parentId) {
        Long maxPos = QueryChain.of(CaseReviewModule.class).select(QueryMethods.max(CASE_REVIEW_MODULE.POS))
                .from(CASE_REVIEW_MODULE).where(CASE_REVIEW_MODULE.PARENT_ID.eq(parentId))
                .oneAs(Long.class);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    private void checkDataValidity(CaseReviewModule caseReviewModule) {
        if (!StringUtils.equals(caseReviewModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在
            boolean exists = QueryChain.of(CaseReviewModule.class).where(CASE_REVIEW_MODULE.ID.eq(caseReviewModule.getParentId())).exists();
            if (!exists) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            //检查项目ID是否和父节点ID一致
            boolean exists1 = QueryChain.of(CaseReviewModule.class).where(CASE_REVIEW_MODULE.ID.eq(caseReviewModule.getParentId())
                    .and(CASE_REVIEW_MODULE.PROJECT_ID.eq(caseReviewModule.getProjectId()))).exists();
            if (!exists1) {
                throw new MSException(Translator.get("project.cannot.match.parent"));
            }
        }
        boolean exists = QueryChain.of(CaseReviewModule.class).where(CASE_REVIEW_MODULE.ID.eq(caseReviewModule.getParentId())
                .and(CASE_REVIEW_MODULE.PROJECT_ID.eq(caseReviewModule.getProjectId()))
                .and(CASE_REVIEW_MODULE.NAME.eq(caseReviewModule.getName()))
                .and(CASE_REVIEW_MODULE.ID.ne(caseReviewModule.getId()))).exists();
        if (exists) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
    }

    private List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = selectIdAndParentIdByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("review.module.default.name"));
    }

    private List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId) {
        return QueryChain.of(CaseReviewModule.class).select(CASE_REVIEW_MODULE.ID,
                        CASE_REVIEW_MODULE.PARENT_ID).from(CASE_REVIEW_MODULE)
                .where(CASE_REVIEW_MODULE.PROJECT_ID.eq(projectId)).listAs(BaseTreeNode.class);
    }

    @Override
    public void updatePos(String id, long pos) {

    }

    @Override
    public void refreshPos(String parentId) {

    }
}
