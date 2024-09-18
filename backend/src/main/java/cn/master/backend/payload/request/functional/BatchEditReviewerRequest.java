package cn.master.backend.payload.request.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class BatchEditReviewerRequest extends BaseReviewCaseBatchRequest {

    @Schema(description = "评审人id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> reviewerId;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append;

}
