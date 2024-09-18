package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.FunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
public class FunctionalCasePageDTO extends FunctionalCase {

    @Schema(description = "自定义字段集合")
    private List<FunctionalCaseCustomFieldDTO> customFields;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "删除人名称")
    private String deleteUserName;


}

