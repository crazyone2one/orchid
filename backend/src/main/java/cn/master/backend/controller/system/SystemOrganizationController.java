package cn.master.backend.controller.system;

import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.Organization;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.OrganizationDTO;
import cn.master.backend.payload.dto.system.ProjectDTO;
import cn.master.backend.payload.dto.system.request.OrganizationRequest;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.system.*;
import cn.master.backend.service.OrganizationService;
import cn.master.backend.service.SystemProjectService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Updated;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 组织 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-07
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "系统设置-系统-组织与项目-组织")
@RequestMapping("/system/organization")
public class SystemOrganizationController {

    private final OrganizationService organizationService;
    private final SystemProjectService systemProjectService;

    /**
     * 添加组织。
     *
     * @param organization 组织
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description = "保存组织")
    public boolean save(@RequestBody @Parameter(description = "组织") Organization organization) {
        return organizationService.save(organization);
    }

    /**
     * 根据主键删除组织。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键组织")
    public boolean remove(@PathVariable @Parameter(description = "组织主键") Serializable id) {
        return organizationService.removeById(id);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织")
    @PreAuthorize("hasPermission('SYSTEM_ORGANIZATION_PROJECT','READ+UPDATE')")
    public void update(@Validated({Updated.class}) @RequestBody OrganizationEditRequest organizationEditRequest) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyProperties(organizationEditRequest, organizationDTO);
        organizationDTO.setUpdateUser(SessionUtils.getCurrentUserId());
        organizationService.update(organizationDTO);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织名称")
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void rename(@Validated({Updated.class}) @RequestBody OrganizationNameEditRequest request) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyProperties(request, organizationDTO);
        organizationDTO.setUpdateUser(SessionUtils.getCurrentUserId());
        organizationService.updateName(organizationDTO);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    //@Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = SystemOrganizationLogService.class)
    public void delete(@PathVariable String id) {
        organizationService.delete(id);
    }

    @GetMapping("/recover/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-恢复组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_RECOVER)
    //@Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = SystemOrganizationLogService.class)
    public void recover(@PathVariable String id) {
        organizationService.recover(id);
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-启用组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        organizationService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-结束组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void disable(@PathVariable String id) {
        organizationService.disable(id);
    }

    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织列表")
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<OrganizationDTO> page(@Validated @RequestBody OrganizationRequest request) {
        return organizationService.list(request);
    }

    @PostMapping("/option/all")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取系统所有组织下拉选项")
    @PreAuthorize("@auth.hasAuthority('SYSTEM_ORGANIZATION_PROJECT:READ') or @auth.hasAuthority('ORGANIZATION_PROJECT:READ') or @auth.hasAuthority('PROJECT_BASE_INFO:READ')")
    public List<OptionDTO> listAll() {
        return organizationService.listOptionAll();
    }

    @PostMapping("/list-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织成员列表")
    @PreAuthorize("@auth.hasAuthority('SYSTEM_ORGANIZATION_PROJECT:READ') or @auth.hasAuthority('SYSTEM_USER:READ')")
    public Page<UserExtendDTO> listMember(@Validated @RequestBody OrganizationRequest request) {
        return organizationService.getMemberListBySystem(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-添加组织成员")
    @HasAuthorize(value = PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    public void addMember(@Validated @RequestBody OrganizationMemberRequest request) {
        organizationService.addMemberBySystem(request, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/list-project")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织下的项目列表")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<ProjectDTO> listProject(@Validated @RequestBody OrganizationProjectRequest request) {
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyProperties(request, projectRequest);
        return systemProjectService.getProjectPage(projectRequest);
    }
    @GetMapping("/total")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织和项目总数")
    @HasAuthorize(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Map<String, Long> getTotal(@RequestParam(value = "organizationId",required = false) String organizationId) {
        return organizationService.getTotal(organizationId);
    }

}
