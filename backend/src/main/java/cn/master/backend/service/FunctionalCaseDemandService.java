package cn.master.backend.service;

import cn.master.backend.payload.dto.functional.FunctionalDemandDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseDemandRequest;
import cn.master.backend.payload.request.functional.QueryDemandListRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.FunctionalCaseDemand;

/**
 * 功能用例和需求的中间表 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
public interface FunctionalCaseDemandService extends IService<FunctionalCaseDemand> {

    Page<FunctionalDemandDTO> pageFunctionalCaseDemands(QueryDemandListRequest request);

    void addDemand(FunctionalCaseDemandRequest request, String userId);
}
