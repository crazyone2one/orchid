package cn.master.backend.service;

import cn.master.backend.constants.TestPlanConstants;
import cn.master.backend.entity.TestPlan;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.TestPlanMapper;
import cn.master.backend.payload.dto.project.MoveNodeSortDTO;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.util.ServiceUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static cn.master.backend.entity.table.TestPlanTableDef.TEST_PLAN;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Service
@RequiredArgsConstructor
public class TestPlanGroupService extends TestPlanSortService {
    private final TestPlanMapper testPlanMapper;
    private static final int MAX_CHILDREN_COUNT = 20;

    @Override
    public long getNextOrder(String groupId) {
        Long maxPos = QueryChain.of(TestPlan.class)
                .select("IF(MAX(pos) IS NULL, 0, MAX(pos)) AS pos")
                .from(TEST_PLAN)
                .where(TestPlan::getGroupId).eq(groupId).oneAs(Long.class);
        return maxPos + ServiceUtils.POS_STEP;
    }

    @Override
    public void updatePos(String id, long pos) {
        UpdateChain.of(TestPlan.class).set(TestPlan::getPos, pos).where(TestPlan::getId).eq(id).update();
    }

    @Override
    public void refreshPos(String testPlanId) {

    }

    public TestPlan validateGroupCapacity(String groupId, int size) {
        // 判断测试计划组是否存在
        TestPlan groupPlan = QueryChain.of(TestPlan.class).where(TestPlan::getId).eq(groupId)
                .oneOpt()
                .orElseThrow(() -> new MSException(Translator.get("test_plan.group.error")));
        if (!StringUtils.equalsIgnoreCase(groupPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }
        //判断并未归档
        if (StringUtils.equalsIgnoreCase(groupPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }
        //判断测试计划组下的测试计划数量是否超过20
        long count = QueryChain.of(TestPlan.class).where(TestPlan::getGroupId).eq(groupId).count();
        if (count + size > 20) {
            throw new MSException(Translator.getWithArgs("test_plan.group.children.max", MAX_CHILDREN_COUNT));
        }
        return groupPlan;
    }

    public void sort(PosRequest request) {
        TestPlan dropPlan = testPlanMapper.selectOneById(request.getMoveId());
        TestPlan targetPlan = testPlanMapper.selectOneById(request.getTargetId());

        // 校验排序的参数 （暂时不支持测试计划的移入移出）
        validateMoveRequest(dropPlan, targetPlan, request.getMoveMode());
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                targetPlan.getGroupId(),
                this.getNodeMoveRequest(request, true),
                testPlanMapper::selectDragInfoById,
                testPlanMapper::selectNodeByPosOperator
        );
        if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
        }
        //判断是否需要刷新排序
        if (this.needRefreshBeforeSort(sortDTO.getPreviousNode(), sortDTO.getNextNode())) {
            this.refreshPos(sortDTO.getSortRangeId());
            //dropPlan = testPlanMapper.selectOneById(request.getMoveId());
            targetPlan = testPlanMapper.selectOneById(request.getTargetId());
            sortDTO = super.getNodeSortDTO(
                    targetPlan.getGroupId(),
                    this.getNodeMoveRequest(request, true),
                    testPlanMapper::selectDragInfoById,
                    testPlanMapper::selectNodeByPosOperator
            );
            if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            }
        }
        this.sort(sortDTO);
    }

    private void validateMoveRequest(TestPlan dropPlan, TestPlan targetPlan, String moveType) {
        //测试计划组不能进行移动操作
        if (dropPlan == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        if (targetPlan == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
    }
}
