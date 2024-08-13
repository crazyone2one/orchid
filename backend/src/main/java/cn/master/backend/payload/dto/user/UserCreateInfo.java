package cn.master.backend.payload.dto.user;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Data
public class UserCreateInfo {
    @Schema(description =  "用户ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String id;

    @Schema(description =  "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    @Email(message = "{user.email.invalid}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(description =  "手机号")
    private String phone;

}
