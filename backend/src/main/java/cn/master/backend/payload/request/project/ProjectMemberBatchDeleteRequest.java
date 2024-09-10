package cn.master.backend.payload.request.project;

import cn.master.backend.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectMemberBatchDeleteRequest extends TableBatchProcessDTO {

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

}
