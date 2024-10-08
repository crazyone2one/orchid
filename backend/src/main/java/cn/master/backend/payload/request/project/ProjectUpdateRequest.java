package cn.master.backend.payload.request.project;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/30/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUpdateRequest {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "项目描述")
    @Size(min = 0, max = 1000, message = "{project.description.length_range}", groups = {Created.class, Updated.class})
    private String description;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project.id.length_range}", groups = {Updated.class})
    private String id;

}