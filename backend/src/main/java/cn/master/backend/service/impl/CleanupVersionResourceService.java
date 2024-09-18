package cn.master.backend.service.impl;

import cn.master.backend.constants.ProjectApplicationType;
import cn.master.backend.entity.ProjectApplication;
import cn.master.backend.entity.ProjectVersion;
import cn.master.backend.mapper.ProjectApplicationMapper;
import cn.master.backend.mapper.ProjectVersionMapper;
import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.util.LogUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
public class CleanupVersionResourceService implements CleanupProjectResourceService {
    @Resource
    ProjectVersionMapper projectVersionMapper;
    @Resource
    ProjectApplicationMapper projectApplicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResources(String projectId) {
        QueryChain<ProjectVersion> eq = QueryChain.of(projectVersionMapper).where(ProjectVersion::getProjectId).eq(projectId);
        LogicDeleteManager.execWithoutLogicDelete(() -> projectVersionMapper.deleteByQuery(eq));
        QueryChain<ProjectApplication> queryChain = QueryChain.of(projectApplicationMapper).where(ProjectApplication::getProjectId).eq(projectId)
                .and(ProjectApplication::getType).eq(ProjectApplicationType.VERSION.VERSION_ENABLE.name());
        LogicDeleteManager.execWithoutLogicDelete(() -> projectApplicationMapper.deleteByQuery(queryChain));
        LogUtils.info("清理当前项目[" + projectId + "]相关版本资源");
    }
}
