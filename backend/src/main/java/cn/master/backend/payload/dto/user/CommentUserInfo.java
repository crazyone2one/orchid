package cn.master.backend.payload.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/09/2024
 **/
@Data
public class CommentUserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "用户ID")
    private String id;

    @Schema(description =  "用户名")
    private String name;

    @Schema(description =  "用户邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;
}
