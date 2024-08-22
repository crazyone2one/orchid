package cn.master.backend.mapper;

import cn.master.backend.entity.TestPlan;
import cn.master.backend.payload.dto.project.DropNode;
import cn.master.backend.payload.dto.project.NodeSortQueryParam;
import com.mybatisflex.core.BaseMapper;

/**
 * 测试计划 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
public interface TestPlanMapper extends BaseMapper<TestPlan> {

    DropNode selectDragInfoById(String s);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
