package cn.master.backend.service.impl;

import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.entity.FunctionalCaseDemand;
import cn.master.backend.mapper.FunctionalCaseDemandMapper;
import cn.master.backend.mapper.FunctionalCaseMapper;
import cn.master.backend.payload.dto.functional.DemandDTO;
import cn.master.backend.payload.dto.functional.FunctionalDemandDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseDemandRequest;
import cn.master.backend.payload.request.functional.FunctionalDemandBatchRequest;
import cn.master.backend.payload.request.functional.QueryDemandListRequest;
import cn.master.backend.service.FunctionalCaseDemandService;
import cn.master.backend.service.ProjectApplicationService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.FunctionalCaseDemandTableDef.FUNCTIONAL_CASE_DEMAND;

/**
 * 功能用例和需求的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class FunctionalCaseDemandServiceImpl extends ServiceImpl<FunctionalCaseDemandMapper, FunctionalCaseDemand> implements FunctionalCaseDemandService {
    private final FunctionalCaseMapper functionalCaseMapper;
    private final ProjectApplicationService projectApplicationService;

    @Override
    public Page<FunctionalDemandDTO> pageFunctionalCaseDemands(QueryDemandListRequest request) {
        Page<FunctionalDemandDTO> page = queryChain()
                .where(FunctionalCaseDemand::getCaseId).eq(request.getCaseId())
                .and(FUNCTIONAL_CASE_DEMAND.PARENT.eq("NONE").or(FUNCTIONAL_CASE_DEMAND.WITH_PARENT.eq(false)))
                .and(FUNCTIONAL_CASE_DEMAND.DEMAND_NAME.like(request.getKeyword()))
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), FunctionalDemandDTO.class);
        Map<String, List<FunctionalDemandDTO>> functionalCaseDemandMap = page.getRecords()
                .stream().
                filter(t -> StringUtils.isNotBlank(t.getDemandId())).collect(Collectors.groupingBy(FunctionalCaseDemand::getDemandId));
        List<String> ids = page.getRecords().stream().map(FunctionalCaseDemand::getId).toList();
        List<FunctionalCaseDemand> functionalCaseDemands = queryChain()
                .where(FunctionalCaseDemand::getId).notIn(ids)
                .and(FunctionalCaseDemand::getCaseId).eq(request.getCaseId())
                .list();
        int lastSize = 0;
        while (CollectionUtils.isNotEmpty(functionalCaseDemands) && functionalCaseDemands.size() != lastSize) {
            lastSize = functionalCaseDemands.size();
            List<FunctionalCaseDemand> notMatchedList = new ArrayList<>();
            for (FunctionalCaseDemand demand : functionalCaseDemands) {
                if (functionalCaseDemandMap.containsKey(demand.getParent())) {
                    FunctionalDemandDTO functionalDemandDTO = new FunctionalDemandDTO();
                    BeanUtils.copyProperties(demand, functionalDemandDTO);
                    functionalCaseDemandMap.get(demand.getParent()).stream().filter(t -> StringUtils.equalsIgnoreCase(t.getDemandPlatform(), demand.getDemandPlatform())).toList().getFirst().addChild(functionalDemandDTO);
                    resetMap(demand, functionalCaseDemandMap, functionalDemandDTO);
                } else {
                    notMatchedList.add(demand);
                }
            }
            functionalCaseDemands = notMatchedList;
        }
        return page;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addDemand(FunctionalCaseDemandRequest request, String userId) {
        FunctionalCase functionalCase = functionalCaseMapper.selectOneById(request.getCaseId());
        List<DemandDTO> demandDTOList = doSelectDemandList(request.getFunctionalDemandBatchRequest(), request.getDemandList(), functionalCase.getProjectId());
        if (demandDTOList.isEmpty()) {
            return;
        }
        List<FunctionalCaseDemand> existDemands = queryChain().where(FunctionalCaseDemand::getCaseId).eq(request.getCaseId())
                .and(FUNCTIONAL_CASE_DEMAND.DEMAND_PLATFORM.eq(request.getDemandPlatform())).list();
        insertDemand(demandDTOList, request, userId, existDemands);
    }

    private void insertDemand(List<DemandDTO> demandDTOList, FunctionalCaseDemandRequest request, String userId, List<FunctionalCaseDemand> existDemands) {

    }

    private List<DemandDTO> doSelectDemandList(FunctionalDemandBatchRequest functionalDemandBatchRequest, List<DemandDTO> demandList, String projectId) {
        if (functionalDemandBatchRequest != null && functionalDemandBatchRequest.isSelectAll()) {
            return List.of();
        }
        else {
            return demandList;
        }
    }

    private void resetMap(FunctionalCaseDemand demand, Map<String, List<FunctionalDemandDTO>> functionalCaseDemandMap, FunctionalDemandDTO functionalDemandDTO) {
        List<FunctionalDemandDTO> functionalDemands = functionalCaseDemandMap.get(demand.getDemandId());
        if (CollectionUtils.isEmpty(functionalDemands)) {
            functionalDemands = new ArrayList<>();
        }
        if (!functionalDemands.contains(functionalDemandDTO)) {
            functionalDemands.add(functionalDemandDTO);
            functionalCaseDemandMap.put(demand.getDemandId(), functionalDemands);
        }
    }
}
