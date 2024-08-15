package cn.master.backend.service;

import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
public interface OrganizationProjectService extends ProjectService {
    ProjectDTO add(AddProjectRequest addProjectDTO, String createUser);

    Page<ProjectDTO> getProjectPage(OrganizationProjectRequest request);

    ProjectDTO update(UpdateProjectRequest updateProjectDto, String updateUser);

    @Override
    int delete(String id, String deleteUser);

    @Override
    ProjectDTO get(String id);

    @Override
    void enable(String id);

    @Override
    void disable(String id);

    @Override
    Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request);

    void addProjectMember(ProjectAddMemberBatchRequest request);

    int removeProjectMember(String projectId, String userId, String createUser);

    List<UserExtendDTO> getUserAdminList(String organizationId, String keyword);

    List<UserExtendDTO> getUserMemberList(String organizationId, String projectId, String keyword);

    @Override
    List<OptionDTO> getTestResourcePoolOptions(ProjectPoolRequest request);

    @Override
    void rename(UpdateProjectNameRequest project, String userId);
}
