package cn.master.backend.service;

import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.ProjectVersion;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 版本管理 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
public interface ProjectVersionService extends IService<ProjectVersion> {

    String getDefaultVersion(String projectId);

    List<ProjectVersion> getVersionByIds(List<String> versionId);
}
