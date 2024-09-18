package cn.master.backend.service.impl;

import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.service.TestPlanService;
import cn.master.backend.util.LogUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
@RequiredArgsConstructor
public class CleanupPlanResourceServiceImpl implements CleanupProjectResourceService {
    private final TestPlanService testPlanService;

    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]的测试计划资源开始");
        testPlanService.deleteByProjectId(projectId);
    }
}
