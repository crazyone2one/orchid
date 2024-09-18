package cn.master.backend.payload.dto.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
public class FunctionalCaseVersionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "项目id")
    private String projectId;
}
