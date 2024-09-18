package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.CaseReviewUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@Data
public class CaseReviewUserDTO extends CaseReviewUser {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "头像")
    private String avatar;
}
