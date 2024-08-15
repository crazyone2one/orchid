package cn.master.backend.service;

import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
public interface SystemProjectService extends ProjectService {
    ProjectDTO add(AddProjectRequest addProjectDTO, String createUser);

    @Override
    ProjectDTO get(String id);

    @Override
    Page<ProjectDTO> getProjectPage(ProjectRequest request);

    ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser);

    @Override
    int delete(String id, String deleteUser);

    @Override
    void enable(String id);

    @Override
    void disable(String id);

    @Override
    Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request);

    void addProjectMember(ProjectAddMemberBatchRequest request);

    int removeProjectMember(String projectId, String userId, String createUser);

    @Override
    List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request);

    @Override
    void rename(UpdateProjectNameRequest project, String userId);

    List<OptionDTO> list(String keyword);
}
