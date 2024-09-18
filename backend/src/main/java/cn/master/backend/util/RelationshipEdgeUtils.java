package cn.master.backend.util;

import cn.master.backend.entity.FunctionalCaseRelationshipEdge;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.payload.dto.system.RelationshipEdgeDTO;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
public class RelationshipEdgeUtils {
    public static void updateGraphId(String id) {
        RelationshipEdgeDTO edge = QueryChain.of(FunctionalCaseRelationshipEdge.class)
                .where(FunctionalCaseRelationshipEdge::getId).eq(id).oneAsOpt(RelationshipEdgeDTO.class)
                .orElseThrow(() -> new MSException(Translator.get("relationship_not_exist")));
        List<RelationshipEdgeDTO> edges = QueryChain.of(FunctionalCaseRelationshipEdge.class)
                .where(FunctionalCaseRelationshipEdge::getGraphId).eq(edge.getGraphId())
                .listAs(RelationshipEdgeDTO.class);
        // 去掉要删除的边
        edges = edges.stream()
                .filter(i -> !i.getSourceId().equals(edge.getSourceId()) && !i.getTargetId().equals(edge.getTargetId()))
                .collect(Collectors.toList());

        Set<String> nodes = new HashSet<>();
        Set<String> markSet = new HashSet<>();
        nodes.addAll(edges.stream().map(RelationshipEdgeDTO::getSourceId).collect(Collectors.toSet()));
        nodes.addAll(edges.stream().map(RelationshipEdgeDTO::getTargetId).collect(Collectors.toSet()));

        dfsForMark(edge.getSourceId(), edges, markSet, true);
        dfsForMark(edge.getSourceId(), edges, markSet, false);

        // 如果连通的点减少，说明形成了两个不连通子图，重新设置graphId
        if (markSet.size() != nodes.size()) {
            List<String> updateIds = new ArrayList<>(markSet);
            UpdateChain.of(FunctionalCaseRelationshipEdge.class)
                    .set(FunctionalCaseRelationshipEdge::getGraphId, UUID.randomUUID().toString())
                    .where(FunctionalCaseRelationshipEdge::getSourceId).in(updateIds)
                    .or(FunctionalCaseRelationshipEdge::getTargetId).in(updateIds)
                    .update();
        }
    }

    private static void dfsForMark(String node, List<RelationshipEdgeDTO> edges, Set<String> markSet, boolean isForwardDirection) {
        markSet.add(node);

        Set<String> nextLevelNodes = new HashSet<>();

        for (RelationshipEdgeDTO edge : edges) {
            if (isForwardDirection) {
                if (node.equals(edge.getSourceId())) {
                    nextLevelNodes.add(edge.getTargetId());
                }
            } else {
                if (node.equals(edge.getTargetId())) {
                    nextLevelNodes.add(edge.getSourceId());
                }
            }
        }

        nextLevelNodes.forEach(nextNode -> {
            if (!markSet.contains(nextNode)) {
                dfsForMark(nextNode, edges, markSet, true);
                dfsForMark(nextNode, edges, markSet, false);
            }
        });
    }
}
