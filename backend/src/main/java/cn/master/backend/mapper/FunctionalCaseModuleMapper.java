package cn.master.backend.mapper;

import cn.master.backend.payload.dto.project.NodeSortQueryParam;
import cn.master.backend.payload.dto.system.BaseModule;
import com.mybatisflex.core.BaseMapper;
import cn.master.backend.entity.FunctionalCaseModule;

/**
 * 功能用例模块 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface FunctionalCaseModuleMapper extends BaseMapper<FunctionalCaseModule> {
    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
