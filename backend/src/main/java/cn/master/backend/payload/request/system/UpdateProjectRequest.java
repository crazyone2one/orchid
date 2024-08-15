package cn.master.backend.payload.request.system;

import cn.master.backend.validation.Updated;
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
public class UpdateProjectRequest extends ProjectBaseRequest {

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project.id.length_range}", groups = {Updated.class})
    private String id;
}

