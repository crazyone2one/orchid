package cn.master.backend.payload.request.system.user;

import cn.master.backend.payload.dto.user.UserCreateInfo;
import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserEditRequest extends UserCreateInfo {
    @Schema(description =  "用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{user_role.id.not_blank}")
    List<@Valid @NotBlank(message = "{user_role.id.not_blank}", groups = {Created.class, Updated.class}) String> userRoleIdList;
}
