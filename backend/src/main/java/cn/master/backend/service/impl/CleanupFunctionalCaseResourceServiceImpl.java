package cn.master.backend.service.impl;

import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.entity.FunctionalCaseModule;
import cn.master.backend.mapper.FunctionalCaseMapper;
import cn.master.backend.mapper.FunctionalCaseModuleMapper;
import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.service.DeleteFunctionalCaseService;
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
public class CleanupFunctionalCaseResourceServiceImpl implements CleanupProjectResourceService {
    private final DeleteFunctionalCaseService deleteFunctionalCaseService;
    private final FunctionalCaseMapper functionalCaseMapper;
    private final FunctionalCaseModuleMapper functionalCaseModuleMapper;

    @Override
    public void deleteResources(String projectId) {
        List<String> ids = QueryChain.of(functionalCaseMapper).where(FunctionalCase::getProjectId).eq(projectId).list()
                .stream().map(FunctionalCase::getId).toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            deleteFunctionalCaseService.deleteFunctionalCaseResource(ids, projectId);
        }
        QueryChain<FunctionalCaseModule> queryChain = QueryChain.of(functionalCaseModuleMapper).where(FunctionalCaseModule::getProjectId).eq(projectId);
        LogicDeleteManager.execWithoutLogicDelete(() -> functionalCaseModuleMapper.deleteByQuery(queryChain));
    }
}
