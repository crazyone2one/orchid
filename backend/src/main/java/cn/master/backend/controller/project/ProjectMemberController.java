package cn.master.backend.controller.project;

import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.project.ProjectUserDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.user.CommentUserInfo;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.project.ProjectMemberAddRequest;
import cn.master.backend.payload.request.project.ProjectMemberAddRoleRequest;
import cn.master.backend.payload.request.project.ProjectMemberBatchDeleteRequest;
import cn.master.backend.payload.request.project.ProjectMemberEditRequest;
import cn.master.backend.payload.request.system.ProjectMemberRequest;
import cn.master.backend.service.ProjectMemberService;
import cn.master.backend.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Tag(name = "项目管理-成员")
@RestController
@RequiredArgsConstructor
@RequestMapping("/project/member")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @PostMapping("/list")
    @Operation(summary = "项目管理-成员-列表查询")
    @HasAuthorize(PermissionConstants.PROJECT_USER_READ)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Page<ProjectUserDTO> listMember(@Validated @RequestBody ProjectMemberRequest request) {
        return projectMemberService.listMember(request);
    }

    @GetMapping("/get-member/option/{projectId}")
    @Operation(summary = "项目管理-成员-获取成员下拉选项")
    @HasAuthorize(PermissionConstants.PROJECT_USER_READ)
    //@CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<UserExtendDTO> getMemberOption(@PathVariable String projectId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return projectMemberService.getMemberOption(projectId, keyword);
    }

    @GetMapping("/get-role/option/{projectId}")
    @Operation(summary = "项目管理-成员-获取用户组下拉选项")
    @HasAuthorize(PermissionConstants.PROJECT_USER_READ)
    //@CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<OptionDTO> getRoleOption(@PathVariable String projectId) {
        return projectMemberService.getRoleOption(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-成员-添加成员")
    @HasAuthorize(PermissionConstants.PROJECT_USER_ADD)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void addMember(@RequestBody ProjectMemberAddRequest request) {
        projectMemberService.addMember(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-成员-编辑成员")
    @HasAuthorize(PermissionConstants.PROJECT_USER_UPDATE)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void updateMember(@RequestBody ProjectMemberEditRequest request) {
        projectMemberService.updateMember(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/remove/{projectId}/{userId}")
    @Operation(summary = "项目管理-成员-移除成员")
    @Parameters({
            @Parameter(name = "projectId", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @HasAuthorize(PermissionConstants.PROJECT_USER_DELETE)
    //@CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void removeMember(@PathVariable String projectId, @PathVariable String userId) {
        projectMemberService.removeMember(projectId, userId, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/add-role")
    @Operation(summary = "项目管理-成员-批量添加至用户组")
    @HasAuthorize(PermissionConstants.PROJECT_USER_UPDATE)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void addMemberRole(@RequestBody ProjectMemberAddRoleRequest request) {
        projectMemberService.addRole(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/batch/remove")
    @Operation(summary = "项目管理-成员-批量从项目移除")
    @HasAuthorize(PermissionConstants.PROJECT_USER_DELETE)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchRemove(@RequestBody ProjectMemberBatchDeleteRequest request) {
        projectMemberService.batchRemove(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/comment/user-option/{projectId}")
    @Operation(summary = "项目管理-成员-获取评论用户@下拉选项")
    public List<CommentUserInfo> selectCommentUser(@PathVariable String projectId, @RequestParam(value = "keyword", required = false) String keyword) {
        return projectMemberService.selectCommentUser(projectId, keyword);
    }

    @PostMapping("/update-member")
    @Operation(summary = "系统设置-系统-组织与项-项目-更新成员用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_UPDATE)
    //@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void updateProjectMemberRole(@RequestBody ProjectMemberEditRequest request) {
        projectMemberService.updateMember(request, SessionUtils.getCurrentUserId());
    }
}
