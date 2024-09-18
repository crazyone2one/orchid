package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.FunctionalCaseModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class FunctionalCaseModuleDTO extends FunctionalCaseModule {
    @Schema(description = "项目名称")
    private String projectName;
}
