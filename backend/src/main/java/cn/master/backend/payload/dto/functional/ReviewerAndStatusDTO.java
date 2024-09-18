package cn.master.backend.payload.dto.functional;

import cn.master.backend.payload.dto.system.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class ReviewerAndStatusDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id")
    private String caseId;

    @Schema(description = "用例评审最终结果")
    private String status;

    @Schema(description = "每个评审人最终的评审结果")
    private List<OptionDTO> reviewerStatus;
}
