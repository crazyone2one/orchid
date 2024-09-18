package cn.master.backend.service.impl;

import cn.master.backend.entity.CaseReview;
import cn.master.backend.entity.CaseReviewModule;
import cn.master.backend.mapper.CaseReviewMapper;
import cn.master.backend.mapper.CaseReviewModuleMapper;
import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.service.DeleteCaseReviewService;
import cn.master.backend.util.LogUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
@RequiredArgsConstructor
public class CleanupCaseReviewResourceServiceImpl implements CleanupProjectResourceService {
    private final CaseReviewMapper caseReviewMapper;
    private final CaseReviewModuleMapper caseReviewModuleMapper;
    private final DeleteCaseReviewService deleteCaseReviewService;

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关用例评审资源");
        List<String> ids = QueryChain.of(caseReviewMapper).where(CaseReview::getProjectId).eq(projectId).list()
                .stream().map(CaseReview::getId).toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            deleteCaseReviewService.deleteCaseReviewResource(ids, projectId);
        }
        //删除模块
        QueryChain<CaseReviewModule> caseReviewModuleQueryChain = QueryChain.of(caseReviewModuleMapper).where(CaseReviewModule::getProjectId).eq(projectId);
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewModuleMapper.deleteByQuery(caseReviewModuleQueryChain));
    }
}
