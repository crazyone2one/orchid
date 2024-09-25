package cn.master.backend.controller.cese;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.handler.annotation.FileLimit;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.functional.FunctionalCaseDetailDTO;
import cn.master.backend.payload.dto.functional.FunctionalCasePageDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseVersionDTO;
import cn.master.backend.payload.dto.project.CustomFieldOptions;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.service.FunctionalCaseService;
import cn.master.backend.service.ProjectTemplateService;
import cn.master.backend.service.log.FunctionalCaseLogService;
import cn.master.backend.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Tag(name = "用例管理-功能用例")
@RestController
@RequiredArgsConstructor
@RequestMapping("/functional/case")
public class FunctionalCaseController {
    private final FunctionalCaseService functionalCaseService;
    private final ProjectTemplateService projectTemplateService;

    @FileLimit
    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例-新增用例")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    public FunctionalCase addFunctionalCase(@Validated @RequestPart("request") FunctionalCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getCurrentUserId();
        String organizationId = SessionUtils.getCurrentOrganizationId();
        return functionalCaseService.addFunctionalCase(request, files, userId, organizationId);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "用例管理-功能用例-查看用例详情")
    @HasAnyAuthorize(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.CASE_REVIEW_READ}, logical = Logical.OR)
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(@PathVariable String id) {
        return functionalCaseService.getFunctionalCaseDetail(id, SessionUtils.getCurrentUserId(), true);
    }

    @GetMapping("/default/template/field/{projectId}")
    @Operation(summary = "用例管理-功能用例-获取默认模板自定义字段")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public TemplateDTO getDefaultTemplateField(@PathVariable String projectId) {
        return projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.FUNCTIONAL.name());
    }

    @FileLimit
    @PostMapping("/update")
    @Operation(summary = "用例管理-功能用例-更新用例")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateFunctionalCaseLog(#request, #files)", logClass = FunctionalCaseLogService.class)
    public FunctionalCase updateFunctionalCase(@Validated @RequestPart("request") FunctionalCaseEditRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return functionalCaseService.updateFunctionalCase(request, files, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-功能用例-关注/取消关注用例")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void editFollower(@Validated @RequestBody FunctionalCaseFollowerRequest request) {
        functionalCaseService.editFollower(request.getFunctionalCaseId(), SessionUtils.getCurrentUserId());
    }

    @GetMapping("/version/{id}")
    @Operation(summary = "用例管理-功能用例-版本信息(用例是否存在多版本)")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<FunctionalCaseVersionDTO> getVersion(@PathVariable @NotBlank(message = "{functional_case.id.not_blank}") String id) {
        return functionalCaseService.getFunctionalCaseVersion(id);
    }

    @PostMapping("/delete")
    @Operation(summary = "用例管理-功能用例-删除用例")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteFunctionalCaseLog(#request)", logClass = FunctionalCaseLogService.class)
    public void deleteFunctionalCase(@Validated @RequestBody FunctionalCaseDeleteRequest request) {
        functionalCaseService.deleteFunctionalCase(request, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-用例列表查询")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public Page<FunctionalCasePageDTO> getFunctionalCasePage(@Validated @RequestBody FunctionalCasePageRequest request) {
        return functionalCaseService.getFunctionalCasePage(request, false, true);
    }

    @PostMapping("/module/count")
    @Operation(summary = "用例管理-功能用例-统计模块数量")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody FunctionalCasePageRequest request) {
        return functionalCaseService.moduleCount(request, false);
    }
    @GetMapping("/custom/field/{projectId}")
    @Operation(summary = "用例管理-功能用例-获取表头自定义字段(高级搜索中的自定义字段)")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<CustomFieldOptions> getTableCustomField(@PathVariable String projectId) {
        return projectTemplateService.getTableCustomField(projectId, TemplateScene.FUNCTIONAL.name());
    }

}
