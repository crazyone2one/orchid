package cn.master.backend.service.impl;

import cn.master.backend.entity.ProjectApplication;
import cn.master.backend.mapper.ProjectApplicationMapper;
import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.service.ScheduleService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
@RequiredArgsConstructor
public class CleanupApplicationResourceServiceImpl implements CleanupProjectResourceService {
    private final ScheduleService scheduleService;
    private final ProjectApplicationMapper projectApplicationMapper;

    @Override
    public void deleteResources(String projectId) {
        scheduleService.deleteByProjectId(projectId);
        QueryChain<ProjectApplication> queryChain = QueryChain.of(projectApplicationMapper).where(ProjectApplication::getProjectId).eq(projectId);
        LogicDeleteManager.execWithoutLogicDelete(() -> projectApplicationMapper.deleteByQuery(queryChain));
    }
}
