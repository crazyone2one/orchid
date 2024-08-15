package cn.master.backend.payload.request.system;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 08/14/2024
 **/
@Data
public class ProjectAddMemberRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description = "用户ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.ids.not_blank}")
    private List<
            @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class, Updated.class})
                    String> userIds;
}
