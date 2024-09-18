package cn.master.backend.mapper;

import cn.master.backend.payload.dto.project.NodeSortQueryParam;
import cn.master.backend.payload.dto.system.BaseModule;
import com.mybatisflex.core.BaseMapper;
import cn.master.backend.entity.CaseReviewModule;

/**
 * 用例评审模块 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
public interface CaseReviewModuleMapper extends BaseMapper<CaseReviewModule> {
    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
