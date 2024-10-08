package cn.master.backend.payload.request.system;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationUserRoleEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "组ID")
    @NotBlank(message = "{user_role.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(description =  "组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "应用范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.scope_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{user_role.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;
}

