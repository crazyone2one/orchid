package cn.master.backend.service.impl;

import cn.master.backend.entity.ProjectVersion;
import cn.master.backend.mapper.ProjectVersionMapper;
import cn.master.backend.service.ProjectVersionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.master.backend.entity.table.ProjectVersionTableDef.PROJECT_VERSION;

/**
 * 版本管理 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectVersionServiceImpl extends ServiceImpl<ProjectVersionMapper, ProjectVersion> implements ProjectVersionService {

    @Override
    public String getDefaultVersion(String projectId) {
        return queryChain().select(PROJECT_VERSION.ID).from(PROJECT_VERSION)
                .where(PROJECT_VERSION.PROJECT_ID.eq(projectId)
                        .and(PROJECT_VERSION.LATEST.eq(true)))
                .limit(1).oneAs(String.class);
    }

    @Override
    public List<ProjectVersion> getVersionByIds(List<String> versionId) {
        return queryChain().where(PROJECT_VERSION.ID.in(versionId)).list();
    }
}
