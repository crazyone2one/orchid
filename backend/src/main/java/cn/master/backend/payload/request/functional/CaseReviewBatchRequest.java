package cn.master.backend.payload.request.functional;

import cn.master.backend.payload.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class CaseReviewBatchRequest extends TableBatchProcessDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "目标模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveModuleId;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "我评审的")
    private String reviewByMe;

    @Schema(description = "我创建的")
    private String createByMe;

}
