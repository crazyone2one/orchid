package cn.master.backend.service.impl;

import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TestPlanModuleMapper;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.project.NodeSortDTO;
import cn.master.backend.payload.request.plan.TestPlanBatchProcessRequest;
import cn.master.backend.payload.request.plan.TestPlanModuleCreateRequest;
import cn.master.backend.payload.request.plan.TestPlanModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.service.ModuleTreeService;
import cn.master.backend.service.TestPlanModuleService;
import cn.master.backend.service.TestPlanService;
import cn.master.backend.service.log.TestPlanModuleLogService;
import cn.master.backend.util.NodeSortUtils;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.master.backend.entity.table.TestPlanModuleTableDef.TEST_PLAN_MODULE;
import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * 测试计划模块 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Service
@RequiredArgsConstructor
public class TestPlanModuleServiceImpl extends ModuleTreeService implements TestPlanModuleService {
    private final TestPlanModuleMapper testPlanModuleMapper;
    private final TestPlanModuleLogService testPlanModuleLogService;
    private final TestPlanService testPlanService;
    protected static final long LIMIT_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

    @Override
    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> baseTreeNodes = QueryChain.of(TestPlanModule.class)
                .where(TEST_PLAN_MODULE.PROJECT_ID.eq(projectId)).orderBy(TEST_PLAN_MODULE.POS.desc()).listAs(BaseTreeNode.class);
        return super.buildTreeAndCountResource(baseTreeNodes, true, Translator.get("unplanned.plan"));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String add(TestPlanModuleCreateRequest request, String operator, String requestUrl, String requestMethod) {
        TestPlanModule testPlanModule = new TestPlanModule();
        testPlanModule.setName(request.getName());
        testPlanModule.setParentId(request.getParentId());
        testPlanModule.setProjectId(request.getProjectId());
        checkDataValidity(testPlanModule);

        testPlanModule.setPos(countPos(request.getParentId()));
        testPlanModule.setCreateUser(operator);
        testPlanModule.setUpdateUser(operator);
        testPlanModuleMapper.insert(testPlanModule);
        testPlanModuleLogService.saveAddLog(testPlanModule, operator, requestUrl, requestMethod);
        return testPlanModule.getId();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void update(TestPlanModuleUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        TestPlanModule module = testPlanModuleMapper.selectOneById(request.getId());
        TestPlanModule updateModule = new TestPlanModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName().trim());
        updateModule.setParentId(module.getParentId());
        updateModule.setProjectId(module.getProjectId());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateUser(userId);
        testPlanModuleMapper.update(updateModule);
        TestPlanModule newModule = testPlanModuleMapper.selectOneById(request.getId());
        //记录日志
        testPlanModuleLogService.saveUpdateLog(module, newModule, module.getProjectId(), userId, requestUrl, requestMethod);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteModule(String deleteId, String operator, String requestUrl, String requestMethod) {
        TestPlanModule deleteModule = testPlanModuleMapper.selectOneById(deleteId);
        if (Objects.nonNull(deleteModule)) {
            deleteModule(Collections.singletonList(deleteId), deleteModule.getProjectId(), operator, requestUrl, requestMethod);
            testPlanModuleLogService.saveDeleteLog(deleteModule, operator, requestUrl, requestMethod);
        }
    }

    @Override
    public void moveNode(NodeMoveRequest request, String currentUser, String requestUrl, String requestMethod) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request, testPlanModuleMapper::selectBaseModuleById, testPlanModuleMapper::selectModuleByParentIdAndPosOperator);
        QueryChain<TestPlanModule> queryChain = QueryChain.of(TestPlanModule.class);
        boolean exists = queryChain.where(TEST_PLAN_MODULE.PARENT_ID.eq(nodeSortDTO.getParent().getId())
                .and(TEST_PLAN_MODULE.NAME.eq(nodeSortDTO.getNode().getName()))
                .and(TEST_PLAN_MODULE.ID.ne(nodeSortDTO.getNode().getId()))).exists();
        if (exists) {
            throw new MSException(Translator.get("test_plan_module_already_exists"));
        }
        queryChain.clear();
        long count = queryChain.where(TEST_PLAN_MODULE.PARENT_ID.eq(nodeSortDTO.getParent().getId())
                .and(TEST_PLAN_MODULE.ID.eq(request.getDragNodeId()))).count();
        if (count == 0) {
            TestPlanModule module = new TestPlanModule();
            module.setId(request.getDragNodeId());
            module.setParentId(nodeSortDTO.getParent().getId());
            testPlanModuleMapper.update(module);
        }
        super.sort(nodeSortDTO);
        //记录日志
        testPlanModuleLogService.saveMoveLog(nodeSortDTO, currentUser, requestUrl, requestMethod);
    }

    @Override
    public Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return super.getIdCountMapByBreadth(treeNodeList);
    }

    private List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = QueryChain.of(TestPlanModule.class)
                .where(TEST_PLAN_MODULE.PROJECT_ID.eq(projectId))
                .listAs(BaseTreeNode.class);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("default.module"));
    }

    public void deleteModule(List<String> deleteIds, String projectId, String operator, String requestUrl, String requestMethod) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        LogicDeleteManager.execWithoutLogicDelete(() -> testPlanModuleMapper.deleteBatchByIds(deleteIds));
        List<String> planDeleteIds = QueryChain.of(TestPlan.class).select(TEST_PLAN.ID).from(TEST_PLAN).where(TEST_PLAN.MODULE_ID.in(deleteIds)).listAs(String.class);
        if (CollectionUtils.isNotEmpty(planDeleteIds)) {
            TestPlanBatchProcessRequest request = new TestPlanBatchProcessRequest();
            request.setModuleIds(deleteIds);
            request.setSelectAll(false);
            request.setProjectId(projectId);
            request.setSelectIds(planDeleteIds);
            testPlanService.batchDelete(request, operator, requestUrl, requestMethod);
        }
        List<String> childrenIds = QueryChain.of(TestPlanModule.class).select(TEST_PLAN_MODULE.ID).from(TEST_PLAN_MODULE).where(TEST_PLAN_MODULE.PARENT_ID.in(deleteIds)).listAs(String.class);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, projectId, operator, requestUrl, requestMethod);
        }
    }

    private Long countPos(String parentId) {
        Long maxPos = QueryChain.of(TestPlanModule.class).select(QueryMethods.max(TEST_PLAN_MODULE.POS)).from(TEST_PLAN_MODULE)
                .where(TEST_PLAN_MODULE.PARENT_ID.eq(parentId)).oneAs(Long.class);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    private void checkDataValidity(TestPlanModule module) {
        if (!StringUtils.equalsIgnoreCase(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            QueryChain<TestPlanModule> queryChain = QueryChain.of(TestPlanModule.class);
            boolean exists = queryChain.where(TEST_PLAN_MODULE.ID.eq(module.getParentId())).exists();
            if (!exists) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            queryChain.clear();
            if (StringUtils.isNotBlank(module.getProjectId())) {
                boolean exists1 = queryChain.where(TEST_PLAN_MODULE.PROJECT_ID.eq(module.getProjectId())
                        .and(TEST_PLAN_MODULE.ID.eq(module.getParentId()))).exists();
                if (!exists1) {
                    throw new MSException(Translator.get("project.cannot.match.parent"));
                }
                queryChain.clear();
            }
        }
        boolean exists = QueryChain.of(TestPlanModule.class).where(TEST_PLAN_MODULE.PARENT_ID.eq(module.getParentId())
                .and(TEST_PLAN_MODULE.PROJECT_ID.eq(module.getProjectId()))
                .and(TEST_PLAN_MODULE.NAME.eq(module.getName()))
                .and(TEST_PLAN_MODULE.ID.ne(module.getId()))).exists();
        if (exists) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        UpdateChain.of(TestPlanModule.class).set(TestPlanModule::getPos, pos)
                .where(TEST_PLAN_MODULE.ID.eq(id)).update();
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = QueryChain.of(TestPlanModule.class)
                .select(TEST_PLAN_MODULE.ID).from(TEST_PLAN_MODULE).where(TEST_PLAN_MODULE.PARENT_ID.eq(parentId))
                .listAs(String.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            UpdateChain.of(TestPlanModule.class).set(TestPlanModule::getPos, (i + 1) * LIMIT_POS)
                    .where(TEST_PLAN_MODULE.ID.eq(nodeId)).update();
        }
    }
}
