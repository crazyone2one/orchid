package cn.master.backend.service;

import cn.master.backend.entity.Project;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 项目 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
public interface ProjectService extends IService<Project> {

    ProjectDTO add(AddProjectRequest request, String createUser, String path, String module);

    ProjectDTO get(String id);

    List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList);

    ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser, String path, String module);

    int delete(String id, String deleteUser);

    void addProjectMember(ProjectAddMemberBatchRequest request, String path, String type, String content, String module);

    int removeProjectMember(String projectId, String userId, String createUser, String module, String path);

    List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request);

    void rename(UpdateProjectNameRequest project, String userId);
    Page<ProjectDTO> getProjectPage(ProjectRequest request);

    void disable(String id);
    void enable(String id);

    Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request);

    int revoke(String id, String currentUserId);
}
