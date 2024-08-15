package cn.master.backend.payload.request.system;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationProjectRequest extends BasePageRequest {
    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}")
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}")
    private String organizationId;
}
