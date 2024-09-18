package cn.master.backend.mapper;

import cn.master.backend.payload.dto.system.RelationshipEdgeDTO;
import com.mybatisflex.core.BaseMapper;
import cn.master.backend.entity.FunctionalCaseRelationshipEdge;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 功能用例的前后置关系 映射层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
public interface FunctionalCaseRelationshipEdgeMapper extends BaseMapper<FunctionalCaseRelationshipEdge> {

}
