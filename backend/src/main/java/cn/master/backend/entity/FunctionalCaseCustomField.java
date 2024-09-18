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
 * 自定义字段功能用例关系 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义字段功能用例关系")
@Table("functional_case_custom_field")
public class FunctionalCaseCustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 资源ID
     */
    @Id
    @Schema(description = "资源ID")
    private String caseId;

    /**
     * 字段ID
     */
    @Id
    @Schema(description = "字段ID")
    private String fieldId;

    /**
     * 字段值
     */
    @Schema(description = "字段值")
    private String value;

}
