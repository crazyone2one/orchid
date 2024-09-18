package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseReviewPageRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "我评审的")
    private String reviewByMe;

    @Schema(description = "我创建的")
    private String createByMe;
}
