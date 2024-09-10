package cn.master.backend.controller.project;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.CustomField;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldUpdateRequest;
import cn.master.backend.service.ProjectCustomFieldService;
import cn.master.backend.service.log.ProjectCustomFieldLogService;
import cn.master.backend.util.SessionUtils;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Tag(name = "项目管理-自定义字段")
@RestController
@RequiredArgsConstructor
@RequestMapping("/project/custom/field")
public class ProjectCustomFieldController {
    private final ProjectCustomFieldService projectCustomFieldService;

    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取自定义字段列表")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<CustomFieldDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String projectId,
                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String scene) {
        return projectCustomFieldService.list(projectId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取自定义字段详情")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_READ)
    public CustomFieldDTO get(@PathVariable String id) {
        return projectCustomFieldService.getCustomFieldWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建自定义字段")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = ProjectCustomFieldLogService.class)
    public CustomField add(@Validated({Created.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        customField.setCreateUser(SessionUtils.getCurrentUserId());
        return projectCustomFieldService.add(customField, request.getOptions());
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义字段")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = ProjectCustomFieldLogService.class)
    public CustomField update(@Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        return projectCustomFieldService.update(customField, request.getOptions());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义字段")
    @HasAuthorize(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = ProjectCustomFieldLogService.class)
    public void delete(@PathVariable String id) {
        projectCustomFieldService.delete(id);
    }
}
