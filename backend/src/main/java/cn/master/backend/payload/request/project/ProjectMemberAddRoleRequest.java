package cn.master.backend.payload.request.project;

import cn.master.backend.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectMemberAddRoleRequest extends TableBatchProcessDTO implements Serializable {

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description =  "用户组ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user_role.id.not_blank}")
    private List<String> roleIds;
}
