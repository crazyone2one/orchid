package cn.master.backend.service;

import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.project.ModuleSortCountResultDTO;
import cn.master.backend.payload.dto.project.NodeSortDTO;
import cn.master.backend.payload.dto.project.NodeSortQueryParam;
import cn.master.backend.payload.dto.system.BaseModule;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import cn.master.backend.util.NodeSortUtils;
import cn.master.backend.util.Translator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public abstract class ModuleTreeService {
    private static final String MOVE_POS_OPERATOR_LESS = "lessThan";
    private static final String MOVE_POS_OPERATOR_MORE = "moreThan";
    private static final String MOVE_POS_OPERATOR_LATEST = "latest";
    private static final String DRAG_NODE_NOT_EXIST = "drag_node.not.exist";

    public BaseTreeNode getDefaultModule(String name) {
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, name, ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
    }

    //构建树结构，并为每个节点计算资源数量
    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, @NotNull List<ModuleCountDTO> moduleCountDTOList, boolean haveVirtualRootNode, String virtualRootName) {
        //构建模块树
        List<BaseTreeNode> baseTreeNodeList = this.buildTreeAndCountResource(traverseList, haveVirtualRootNode, virtualRootName);
        //构建模块节点统计的数据结构
        Map<String, Integer> resourceCountMap = moduleCountDTOList.stream().collect(Collectors.toMap(ModuleCountDTO::getModuleId, ModuleCountDTO::getDataCount));
        //为每个节点赋值资源数量
        sumModuleResourceCount(baseTreeNodeList, resourceCountMap);
        return baseTreeNodeList;
    }

    private void sumModuleResourceCount(List<BaseTreeNode> baseTreeNodeList, Map<String, Integer> resourceCountMap) {
        for (BaseTreeNode node : baseTreeNodeList) {
            //赋值子节点的资源数量
            this.sumModuleResourceCount(node.getChildren(), resourceCountMap);
            //当前节点的资源数量（包含子节点）
            long childResourceCount = 0;
            for (BaseTreeNode childNode : node.getChildren()) {
                childResourceCount += childNode.getCount();
            }
            if (resourceCountMap.containsKey(node.getId())) {
                node.setCount(childResourceCount + resourceCountMap.get(node.getId()));
            } else {
                node.setCount(childResourceCount);
            }
        }
    }

    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode, String virtualRootName) {
        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        if (haveVirtualRootNode) {
            BaseTreeNode defaultNode = getDefaultModule(virtualRootName);
            defaultNode.genModulePath(null);
            baseTreeNodeList.add(defaultNode);
        }
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            lastSize = traverseList.size();
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (!baseTreeNodeMap.containsKey(treeNode.getParentId()) && !StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    notMatchedList.add(treeNode);
                    continue;
                }
                BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                node.genModulePath(baseTreeNodeMap.get(treeNode.getParentId()));
                baseTreeNodeMap.put(treeNode.getId(), node);

                if (StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    baseTreeNodeList.add(node);
                } else if (baseTreeNodeMap.containsKey(treeNode.getParentId())) {
                    baseTreeNodeMap.get(treeNode.getParentId()).addChild(node);
                }
            }
            traverseList = notMatchedList;
        }
        return baseTreeNodeList;
    }


    public NodeSortDTO getNodeSortDTO(NodeMoveRequest request, Function<String, BaseModule> selectIdNodeFunc, Function<NodeSortQueryParam, BaseModule> selectPosNodeFunc) {
        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
            //两种节点不能一样
            throw new MSException(Translator.get("invalid_parameter") + ": drag node  and drop node");
        }
        BaseModule dragNode = selectIdNodeFunc.apply(request.getDragNodeId());
        if (dragNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDragNodeId());
        }
        BaseModule dropNode = selectIdNodeFunc.apply(request.getDropNodeId());
        if (dropNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDropNodeId());

        }
        BaseModule parentModule;
        BaseModule previousNode;
        BaseModule nextNode = null;
        if (request.getDropPosition() == 0) {
            //dropPosition=0: 放到dropNode节点内，最后一个节点之后
            parentModule = new BaseModule(dropNode.getId(), dropNode.getName(), dropNode.getPos(), dropNode.getProjectId(), dropNode.getParentId());

            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setParentId(dropNode.getId());
            sortParam.setOperator(MOVE_POS_OPERATOR_LATEST);
            previousNode = selectPosNodeFunc.apply(sortParam);
        } else {
            if (StringUtils.equalsIgnoreCase(dropNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                parentModule = new BaseModule(ModuleConstants.ROOT_NODE_PARENT_ID, ModuleConstants.ROOT_NODE_PARENT_ID, 0, dragNode.getProjectId(), ModuleConstants.ROOT_NODE_PARENT_ID);
            } else {
                parentModule = selectIdNodeFunc.apply(dropNode.getParentId());
            }
            if (request.getDropPosition() == 1) {
                //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
                previousNode = dropNode;

                NodeSortQueryParam sortParam = new NodeSortQueryParam();
                sortParam.setParentId(parentModule.getId());
                sortParam.setPos(previousNode.getPos());
                sortParam.setOperator(MOVE_POS_OPERATOR_MORE);
                nextNode = selectPosNodeFunc.apply(sortParam);
            } else if (request.getDropPosition() == -1) {
                //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
                nextNode = dropNode;

                NodeSortQueryParam sortParam = new NodeSortQueryParam();
                sortParam.setParentId(parentModule.getId());
                sortParam.setPos(nextNode.getPos());
                sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
                previousNode = selectPosNodeFunc.apply(sortParam);
            } else {
                throw new MSException(Translator.get("invalid_parameter") + ": dropPosition");
            }
        }

        return new NodeSortDTO(dragNode, parentModule, previousNode, nextNode);
    }


    public void sort(@Validated NodeSortDTO nodeMoveDTO) {
        // 获取相邻节点
        BaseModule previousNode = nodeMoveDTO.getPreviousNode();
        BaseModule nextNode = nodeMoveDTO.getNextNode();

        ModuleSortCountResultDTO countResultDTO = NodeSortUtils.countModuleSort(
                previousNode == null ? -1 : previousNode.getPos(),
                nextNode == null ? -1 : nextNode.getPos());
        updatePos(nodeMoveDTO.getNode().getId(), countResultDTO.getPos());
        if (countResultDTO.isRefreshPos()) {
            refreshPos(nodeMoveDTO.getParent().getId());
        }
    }

    public abstract void updatePos(String id, long pos);

    public abstract void refreshPos(String parentId);

    public Map<String, Long> getIdCountMapByBreadth(List<BaseTreeNode> treeNodeList) {
        Map<String, Long> returnMap = new HashMap<>();
        List<BaseTreeNode> whileList = new ArrayList<>(treeNodeList);
        while (CollectionUtils.isNotEmpty(whileList)) {
            List<BaseTreeNode> childList = new ArrayList<>();
            for (BaseTreeNode treeNode : whileList) {
                returnMap.put(treeNode.getId(), treeNode.getCount());
                childList.addAll(treeNode.getChildren());
            }
            whileList = childList;
        }
        return returnMap;
    }
}
