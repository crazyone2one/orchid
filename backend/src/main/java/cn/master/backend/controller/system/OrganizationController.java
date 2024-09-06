package cn.master.backend.controller.system;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.OptionDisabledDTO;
import cn.master.backend.payload.dto.system.OrgUserExtend;
import cn.master.backend.payload.dto.system.request.OrganizationRequest;
import cn.master.backend.payload.request.system.OrgMemberExtendProjectRequest;
import cn.master.backend.payload.request.system.OrganizationMemberExtendRequest;
import cn.master.backend.payload.request.system.OrganizationMemberUpdateRequest;
import cn.master.backend.service.OrganizationService;
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
 * @author Created by 11's papa on 09/06/2024
 **/
@Tag(name = "系统设置-组织-成员")
@RestController
@RequiredArgsConstructor
@RequestMapping("/organization")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping("/member/list")
    @Operation(summary = "系统设置-组织-成员-获取组织成员列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_READ)
    public Page<OrgUserExtend> getMemberList(@Validated @RequestBody OrganizationRequest organizationRequest) {
        return organizationService.getMemberListByOrg(organizationRequest);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public void addMemberByList(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberByOrg(organizationMemberExtendRequest, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/role/update-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至用户组")
    @HasAuthorize(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberRole(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberRole(organizationMemberExtendRequest, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/update-member")
    @Operation(summary = "系统设置-组织-成员-更新用户")
    @HasAnyAuthorize(value = {PermissionConstants.ORGANIZATION_MEMBER_UPDATE, PermissionConstants.PROJECT_USER_READ_ADD, PermissionConstants.PROJECT_USER_READ_DELETE}, logical = Logical.OR)
    public void updateMember(@Validated @RequestBody OrganizationMemberUpdateRequest organizationMemberExtendRequest) {
        organizationService.updateMember(organizationMemberExtendRequest, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/project/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至项目")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberToProject(@Validated @RequestBody OrgMemberExtendProjectRequest orgMemberExtendProjectRequest) {
        organizationService.addMemberToProject(orgMemberExtendProjectRequest, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-组织-成员-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.batchDelLog(#organizationId, #userId)", logClass = OrganizationService.class)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/project/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有项目")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据项目名查询", requiredMode = Schema.RequiredMode.REQUIRED) @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getProjectList(organizationId, keyword);
    }

    @GetMapping("/user/role/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有自定义用户组以及组织级别的用户组")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_MEMBER_READ)
    public List<OptionDTO> getUserRoleList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserRoleList(organizationId);
    }

    @GetMapping("/not-exist/user/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取不在当前组织的所有用户")
    @HasAnyAuthorize(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public List<OptionDisabledDTO> getUserList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getUserList(organizationId, keyword);
    }
}
