package cn.master.backend.payload.dto.functional;

import cn.master.backend.payload.dto.system.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectOptionDTO extends OptionDTO {

    @Schema(description = "是否是默认模板")
    private String projectName;
}
