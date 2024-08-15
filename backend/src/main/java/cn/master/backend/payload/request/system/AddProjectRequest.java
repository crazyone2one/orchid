package cn.master.backend.payload.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class AddProjectRequest extends ProjectBaseRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 1, max = 50, message = "{project.id.length_range}")
    private String id;
}
