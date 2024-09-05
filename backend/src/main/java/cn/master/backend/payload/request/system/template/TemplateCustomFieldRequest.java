package cn.master.backend.payload.request.system.template;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/04/2024
 **/
@Data
public class TemplateCustomFieldRequest {

    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template_custom_field.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(title = "是否必填")
    private Boolean required;

    @Schema(title = "api字段名")
    private String apiFieldId;

    @Schema(title = "默认值")
    private Object defaultValue;
}
