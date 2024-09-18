package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.CaseReviewHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class CaseReviewHistoryDTO extends CaseReviewHistory {

    @Schema(description =  "评审人头像")
    private String userLogo;

    @Schema(description =  "评审人名")
    private String userName;

    @Schema(description =  "评审人邮箱")
    private String email;

    @Schema(description =  "评审解析内容")
    private String contentText;

    @Schema(description =  "评审名称")
    private String reviewName;

}
