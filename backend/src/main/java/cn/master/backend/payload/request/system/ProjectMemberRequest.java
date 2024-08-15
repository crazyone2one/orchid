package cn.master.backend.payload.request.system;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectMemberRequest extends BasePageRequest {
    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

}
