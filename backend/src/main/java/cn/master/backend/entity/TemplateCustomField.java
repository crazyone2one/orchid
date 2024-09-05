package cn.master.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 模板和字段的关联关系 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板和字段的关联关系")
@Table("template_custom_field")
public class TemplateCustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 字段ID
     */
    @Schema(description = "字段ID")
    private String fieldId;

    /**
     * 模版ID
     */
    @Schema(description = "模版ID")
    private String templateId;

    /**
     * 是否必填
     */
    @Schema(description = "是否必填")
    private Boolean required;

    /**
     * 是否是系统字段
     */
    @Schema(description = "是否是系统字段")
    private Boolean systemField;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private Integer pos;

    /**
     * api字段名
     */
    @Schema(description = "api字段名")
    private String apiFieldId;

    /**
     * 默认值
     */
    @Schema(description = "默认值")
    private String defaultValue;

}
