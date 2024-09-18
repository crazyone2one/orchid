package cn.master.backend.service.impl;

import cn.master.backend.entity.Bug;
import cn.master.backend.mapper.BugMapper;
import cn.master.backend.service.BugCommonService;
import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.util.LogUtils;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
@RequiredArgsConstructor
public class CleanupBugResourceServiceImpl implements CleanupProjectResourceService {
    private final BugMapper bugMapper;
    private final BugCommonService bugCommonService;
    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关缺陷测试资源");
        List<String> deleteIds = QueryChain.of(bugMapper).where(Bug::getProjectId).eq(projectId).list()
                .stream().map(Bug::getId).toList();
        if (!deleteIds.isEmpty()) {
            bugMapper.deleteBatchByIds(deleteIds);
            // 清空关联关系
            bugCommonService.clearAssociateResource(projectId, deleteIds);
        }
    }
}
