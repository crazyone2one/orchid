package cn.master.backend.service;

import cn.master.backend.payload.request.project.ProjectApplicationRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.ProjectApplication;

import java.util.List;
import java.util.Map;

/**
 * 项目应用 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
public interface ProjectApplicationService extends IService<ProjectApplication> {

    Map<String, Object> get(ProjectApplicationRequest request, List<String> types);

    void putResourcePool(String projectId, Map<String, Object> configMap, String type);

    ProjectApplication getByType(String projectId, String name);

    void createOrUpdateConfig(ProjectApplication projectApplication);
}
