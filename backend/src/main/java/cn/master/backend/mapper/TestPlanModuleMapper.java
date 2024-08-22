package cn.master.backend.mapper;

import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.payload.dto.project.NodeSortQueryParam;
import cn.master.backend.payload.dto.system.BaseModule;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * 测试计划模块 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
public interface TestPlanModuleMapper extends BaseMapper<TestPlanModule> {
    @Select("SELECT id, name, pos, project_Id, parent_id from test_plan_module where id=#{dragNodeId}")
    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
