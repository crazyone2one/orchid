package cn.master.backend.payload.dto.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class ReviewsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id")
    private String caseId;

    @Schema(description = "用例评审人id字符串集合")
    private String userIds;

    @Schema(description = "用例评审人名称字符集合")
    private String userNames;
}
