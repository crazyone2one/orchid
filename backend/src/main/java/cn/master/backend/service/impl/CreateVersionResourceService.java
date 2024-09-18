package cn.master.backend.service.impl;

import cn.master.backend.constants.InternalUserRole;
import cn.master.backend.constants.ProjectApplicationType;
import cn.master.backend.entity.ProjectApplication;
import cn.master.backend.entity.ProjectVersion;
import cn.master.backend.mapper.ProjectApplicationMapper;
import cn.master.backend.mapper.ProjectVersionMapper;
import cn.master.backend.service.CreateProjectResourceService;
import cn.master.backend.service.ProjectApplicationService;
import cn.master.backend.util.LogUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
public class CreateVersionResourceService implements CreateProjectResourceService {
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;

    public static final String DEFAULT_VERSION = "v1.0";
    public static final String DEFAULT_VERSION_STATUS = "open";

    @Override
    public void createResources(String projectId) {
        ProjectVersion projectVersion = ProjectVersion.builder()
                .projectId(projectId)
                .name(DEFAULT_VERSION)
                .status(DEFAULT_VERSION_STATUS)
                .latest(true)
                .createUser(InternalUserRole.ADMIN.getValue())
                .build();
        projectVersionMapper.insert(projectVersion);
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setType(ProjectApplicationType.VERSION.VERSION_ENABLE.name());
        projectApplication.setTypeValue("FALSE");
        projectApplicationService.update(projectApplication,"");
        LogUtils.info("初始化当前项目[" + projectId + "]相关版本资源");
    }
}
