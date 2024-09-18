package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class ReviewFunctionalCasePageRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.case_review_id.not_blank}")
    private String reviewId;

    @Schema(description = "用例所在项目ID(默认当前项目)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "我的评审结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewStatusFlag;
}
