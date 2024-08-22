package cn.master.backend.service;

import cn.master.backend.entity.TestPlanCollection;
import cn.master.backend.payload.dto.plan.TestPlanResourceExecResultDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
public abstract class TestPlanResourceService extends TestPlanSortService {
    public abstract void deleteBatchByTestPlanId(List<String> testPlanIdList);

    public abstract Map<String, Long> caseExecResultCount(String testPlanId);

    public abstract List<TestPlanResourceExecResultDTO> selectDistinctExecResultByProjectId(String projectId);

    public abstract void initResourceDefaultCollection(String planId, List<TestPlanCollection> defaultCollections);

    public abstract long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator);
}
