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
 * 缺陷自定义字段 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缺陷自定义字段")
@Table("bug_custom_field")
public class BugCustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private String id;

    /**
     * 缺陷ID
     */
    @Id
    @Schema(description = "缺陷ID")
    private String bugId;

    /**
     * 字段ID
     */
    @Id
    @Schema(description = "字段ID")
    private String fieldId;

    @Schema(description = "")
    private String value;

}
