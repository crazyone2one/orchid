package cn.master.backend.payload.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Data
@Schema(description =  "用户组与用户的关联关系DTO")
public class UserRoleRelationUserDTO {

    @Schema(description =  "关联关系ID")
    private String id;

    @Schema(description =  "用户ID")
    private String userId;

    @Schema(description =  "用户名")
    private String name;

    @Schema(description =  "用户邮箱")
    private String email;

    @Schema(description =  "手机号")
    private String phone;
}
