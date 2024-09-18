package cn.master.backend.payload.dto.functional;

import cn.master.backend.payload.dto.project.ModuleCountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class FunctionalCaseModuleCountDTO extends ModuleCountDTO {
    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "项目名称")
    private String projectName;
}

