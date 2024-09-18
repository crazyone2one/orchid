package cn.master.backend.service.impl;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.entity.FunctionalCaseModule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.FunctionalCaseModuleMapper;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.NodeSortDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseModuleCreateRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.ModuleTreeService;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.master.backend.entity.table.FunctionalCaseModuleTableDef.FUNCTIONAL_CASE_MODULE;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;

/**
 * 功能用例模块 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
@RequiredArgsConstructor
public class FunctionalCaseModuleServiceImpl extends ModuleTreeService {
    private final FunctionalCaseModuleMapper functionalCaseModuleMapper;
    private final OperationLogService operationLogService;

    @Override
    public void updatePos(String id, long pos) {

    }

    @Override
    public void refreshPos(String parentId) {

    }

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> functionalModuleList = QueryChain.of(FunctionalCaseModule.class)
                .select(FUNCTIONAL_CASE_MODULE.ID, FUNCTIONAL_CASE_MODULE.NAME,
                        FUNCTIONAL_CASE_MODULE.PARENT_ID)
                .select("'module' AS type").from(FUNCTIONAL_CASE_MODULE)
                .where(FUNCTIONAL_CASE_MODULE.PROJECT_ID.eq(projectId)).orderBy(FUNCTIONAL_CASE_MODULE.POS.desc())
                .listAs(BaseTreeNode.class);
        return super.buildTreeAndCountResource(functionalModuleList, true, Translator.get("functional_case.module.default.name"));
    }

    @Transactional(rollbackOn = Exception.class)
    public String add(FunctionalCaseModuleCreateRequest request, String currentUserId) {
        FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
        functionalCaseModule.setName(request.getName());
        functionalCaseModule.setParentId(request.getParentId());
        functionalCaseModule.setProjectId(request.getProjectId());
        checkDataValidity(functionalCaseModule);
        functionalCaseModule.setPos(countPos(request.getParentId()));
        functionalCaseModule.setCreateUser(currentUserId);
        functionalCaseModule.setUpdateUser(currentUserId);
        functionalCaseModuleMapper.insert(functionalCaseModule);
        return functionalCaseModule.getId();
    }

    @Transactional(rollbackOn = Exception.class)
    public void update(FunctionalCaseModuleUpdateRequest request, String currentUserId) {
        FunctionalCaseModule functionalCaseModule = functionalCaseModuleMapper.selectOneById(request.getId());
        if (functionalCaseModule == null) {
            throw new MSException(Translator.get("case_module.not.exist"));
        }
        functionalCaseModule.setName(request.getName());
        this.checkDataValidity(functionalCaseModule);
        functionalCaseModule.setUpdateUser(currentUserId);
        functionalCaseModuleMapper.update(functionalCaseModule);
    }

    @Transactional(rollbackOn = Exception.class)
    public void moveNode(NodeMoveRequest request, String currentUserId) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                functionalCaseModuleMapper::selectBaseModuleById,
                functionalCaseModuleMapper::selectModuleByParentIdAndPosOperator);
        long count = QueryChain.of(FunctionalCaseModule.class).where(FUNCTIONAL_CASE_MODULE.PARENT_ID.eq(nodeSortDTO.getParent().getId())
                .and(FUNCTIONAL_CASE_MODULE.ID.eq(request.getDragNodeId()))).count();
        if (count == 0) {
            FunctionalCaseModule moveModule = functionalCaseModuleMapper.selectOneById(request.getDragNodeId());
            moveModule.setParentId(nodeSortDTO.getParent().getId());
            this.checkDataValidity(moveModule);
            FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
            functionalCaseModule.setId(request.getDragNodeId());
            functionalCaseModule.setParentId(nodeSortDTO.getParent().getId());
            functionalCaseModule.setUpdateUser(currentUserId);
            functionalCaseModuleMapper.update(functionalCaseModule);
        }
        super.sort(nodeSortDTO);
    }

    @Transactional(rollbackOn = Exception.class)
    public void deleteModule(String moduleId, String currentUserId) {
        FunctionalCaseModule deleteModule = functionalCaseModuleMapper.selectOneById(moduleId);
        if (deleteModule != null) {
            List<FunctionalCase> functionalCases = deleteModuleByIds(Collections.singletonList(moduleId), new ArrayList<>(), currentUserId);
            batchDelLog(functionalCases, deleteModule.getProjectId());
            //List<String> ids = functionalCases.stream().map(FunctionalCase::getId).toList();
            //User user = userMapper.selectByPrimaryKey(currentUserId);
            //functionalCaseNoticeService.batchSendNotice(deleteModule.getProjectId(), ids, user, NoticeConstants.Event.DELETE);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void batchDelLog(List<FunctionalCase> functionalCases, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        functionalCases.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    item.getName());

            dto.setPath("/functional/case/module/delete/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }


    public String getModuleName(String moduleId) {
        if (ModuleConstants.DEFAULT_NODE_ID.equals(moduleId)) {
            return Translator.get("functional_case.module.default.name");
        }
        return functionalCaseModuleMapper.selectOneById(moduleId).getName();
    }

    private List<FunctionalCase> deleteModuleByIds(List<String> deleteIds, List<FunctionalCase> functionalCases, String userId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return functionalCases;
        }
        QueryChain<FunctionalCaseModule> queryChain = QueryChain.of(FunctionalCaseModule.class).where(FUNCTIONAL_CASE_MODULE.ID.in(deleteIds));
        LogicDeleteManager.execWithoutLogicDelete(() -> functionalCaseModuleMapper.deleteByQuery(queryChain));
        List<FunctionalCase> functionalCaseList = QueryChain.of(FunctionalCase.class)
                .where(FUNCTIONAL_CASE.MODULE_ID.in(deleteIds))
                .list();
        if (CollectionUtils.isNotEmpty(functionalCaseList)) {
            functionalCases.addAll(functionalCaseList);
        }
        removeToTrashByModuleIds(deleteIds, userId);
        List<String> childrenIds = QueryChain.of(FunctionalCaseModule.class)
                .select(FUNCTIONAL_CASE_MODULE.ID).from(FUNCTIONAL_CASE_MODULE)
                .where(FUNCTIONAL_CASE_MODULE.PARENT_ID.in(deleteIds))
                .listAs(String.class);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds, functionalCases, userId);
        }
        return functionalCases;
    }

    private void removeToTrashByModuleIds(List<String> deleteIds, String userId) {
        UpdateChain.of(FunctionalCase.class)
                .set(FUNCTIONAL_CASE.DELETED, true)
                .set(FUNCTIONAL_CASE.MODULE_ID, "root")
                .set(FUNCTIONAL_CASE.DELETE_TIME, LocalDateTime.now())
                .set(FUNCTIONAL_CASE.DELETE_USER, userId)
                .where(FUNCTIONAL_CASE.MODULE_ID.in(deleteIds)).update();
    }

    private Long countPos(String parentId) {
        Long maxPos = QueryChain.of(FunctionalCaseModule.class).select(QueryMethods.max(FUNCTIONAL_CASE_MODULE.POS))
                .from(FUNCTIONAL_CASE_MODULE)
                .where(FUNCTIONAL_CASE_MODULE.PARENT_ID.eq(parentId)).oneAs(Long.class);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    private void checkDataValidity(FunctionalCaseModule functionalCaseModule) {
        QueryChain<FunctionalCaseModule> queryChain = QueryChain.of(FunctionalCaseModule.class);
        if (!StringUtils.equals(functionalCaseModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            boolean exists = queryChain.where(FUNCTIONAL_CASE_MODULE.ID.eq(functionalCaseModule.getParentId())).exists();
            if (!exists) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            queryChain.clear();
            boolean exists1 = queryChain.where(FUNCTIONAL_CASE_MODULE.PROJECT_ID.eq(functionalCaseModule.getProjectId())
                    .and(FUNCTIONAL_CASE_MODULE.ID.eq(functionalCaseModule.getParentId()))).exists();
            if (!exists1) {
                throw new MSException(Translator.get("project.cannot.match.parent"));
            }
            queryChain.clear();
        }
        boolean exists = queryChain.where(FUNCTIONAL_CASE_MODULE.PARENT_ID.eq(functionalCaseModule.getParentId())
                .and(FUNCTIONAL_CASE_MODULE.NAME.eq(functionalCaseModule.getName()))
                .and(FUNCTIONAL_CASE_MODULE.PROJECT_ID.eq(functionalCaseModule.getProjectId()))
                .and(FUNCTIONAL_CASE_MODULE.ID.ne(functionalCaseModule.getId()))).exists();
        if (exists) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
    }

    public List<BaseTreeNode> getNodeByNodeIds(List<String> moduleIds) {
        List<String> finalModuleIds = new ArrayList<>(moduleIds);
        List<BaseTreeNode> totalList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(finalModuleIds)) {
            List<BaseTreeNode> modules = QueryChain.of(functionalCaseModuleMapper)
                    .select(FUNCTIONAL_CASE_MODULE.ID, FUNCTIONAL_CASE_MODULE.NAME, FUNCTIONAL_CASE_MODULE.PARENT_ID)
                    .select("'module' AS type").from(FUNCTIONAL_CASE_MODULE)
                    .where(FUNCTIONAL_CASE_MODULE.ID.in(finalModuleIds))
                    .listAs(BaseTreeNode.class);
            totalList.addAll(modules);
            List<String> finalModuleIdList = finalModuleIds;
            List<String> parentModuleIds = modules.stream().map(BaseTreeNode::getParentId)
                    .filter(parentId -> !StringUtils.equalsIgnoreCase(parentId, ModuleConstants.ROOT_NODE_PARENT_ID) && !finalModuleIdList.contains(parentId))
                    .toList();
            finalModuleIds.clear();
            finalModuleIds = new ArrayList<>(parentModuleIds);
        }
        return totalList.stream().distinct().toList();
    }
}
