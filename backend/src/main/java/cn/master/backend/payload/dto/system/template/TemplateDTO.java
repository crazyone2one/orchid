package cn.master.backend.payload.dto.system.template;

import cn.master.backend.entity.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateDTO extends Template {
    @Schema(description = "是否平台默认模板")
    private Boolean platformDefault;
    @Schema(description = "模板关联的自定义字段")
    List<TemplateCustomFieldDTO> customFields;
    @Schema(description = "系统字段配置")
    List<TemplateCustomFieldDTO> systemFields;
}
