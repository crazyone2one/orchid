package cn.master.backend.controller.system;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.CustomField;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.handler.annotation.Log;
import cn.master.backend.payload.dto.system.CustomFieldDTO;
import cn.master.backend.payload.request.system.CustomFieldUpdateRequest;
import cn.master.backend.service.OrganizationCustomFieldService;
import cn.master.backend.service.log.OrganizationCustomFieldLogService;
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
 * @author Created by 11's papa on 09/06/2024
 **/
@Tag(name = "系统设置-组织-自定义字段")
@RestController
@RequiredArgsConstructor
@RequestMapping("/organization/custom/field")
public class OrganizationCustomFieldController {
    private final OrganizationCustomFieldService organizationCustomFieldService;

    @PostMapping("/add")
    @Operation(summary = "创建自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#logClass.addLog(#request)", logClass = OrganizationCustomFieldLogService.class)
    public CustomField add(@Validated({Created.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        customField.setCreateUser(SessionUtils.getCurrentUserId());
        return organizationCustomFieldService.add(customField, request.getOptions());
    }

    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取自定义字段列表")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<CustomFieldDTO> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String organizationId,
                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                     @PathVariable String scene) {
        return organizationCustomFieldService.list(organizationId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取自定义字段详情")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public CustomFieldDTO get(@PathVariable String id) {
        return organizationCustomFieldService.getCustomFieldWithCheck(id);
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#logClass.updateLog(#request)", logClass = OrganizationCustomFieldLogService.class)
    public CustomField update(@Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        return organizationCustomFieldService.update(customField, request.getOptions());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义字段")
    @HasAuthorize(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#logClass.deleteLog(#id)", logClass = OrganizationCustomFieldLogService.class)
    public void delete(@PathVariable String id) {
        organizationCustomFieldService.delete(id);
    }
}
