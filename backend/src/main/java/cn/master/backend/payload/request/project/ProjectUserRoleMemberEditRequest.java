package cn.master.backend.payload.request.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/05/2024
 **/

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserRoleMemberEditRequest implements Serializable {

    @Schema(description =  "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}")
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}")
    private String userRoleId;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    @Size(min = 1, max = 50, message = "{project.id.length_range}")
    private String projectId;

    @Schema(description =  "成员ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.id.not_blank}")
    private List<String> userIds;
}

