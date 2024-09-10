package cn.master.backend.service;

import cn.master.backend.payload.dto.project.ProjectUserDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.user.CommentUserInfo;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.project.ProjectMemberAddRequest;
import cn.master.backend.payload.request.project.ProjectMemberAddRoleRequest;
import cn.master.backend.payload.request.project.ProjectMemberBatchDeleteRequest;
import cn.master.backend.payload.request.project.ProjectMemberEditRequest;
import cn.master.backend.payload.request.system.ProjectMemberRequest;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
public interface ProjectMemberService {
    Page<ProjectUserDTO> listMember(ProjectMemberRequest request);

    List<UserExtendDTO> getMemberOption(String projectId, String keyword);

    List<OptionDTO> getRoleOption(String projectId);

    void addMember(ProjectMemberAddRequest request, String currentUserId);

    void updateMember(ProjectMemberEditRequest request, String currentUserId);

    void removeMember(String projectId, String userId, String currentUserId);

    void addRole(ProjectMemberAddRoleRequest request, String currentUserId);

    void batchRemove(ProjectMemberBatchDeleteRequest request, String currentUserId);

    List<CommentUserInfo> selectCommentUser(String projectId, String keyword);
}
