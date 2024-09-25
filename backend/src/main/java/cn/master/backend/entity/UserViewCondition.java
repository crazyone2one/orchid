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
 * 用户视图条件项 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户视图条件项")
@Table("user_view_condition")
public class UserViewCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 条件ID
     */
    @Id
    @Schema(description = "条件ID")
    private String id;

    /**
     * 视图ID
     */
    @Schema(description = "视图ID")
    private String userViewId;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    private String name;

    /**
     * 查询的期望值
     */
    @Schema(description = "查询的期望值")
    private String value;

    /**
     * 期望值的数据类型：STRING,INT,FLOAT,ARRAY
     */
    @Schema(description = "期望值的数据类型：STRING,INT,FLOAT,ARRAY")
    private String valueType;

    /**
     * 是否为自定义字段
     */
    @Schema(description = "是否为自定义字段")
    private Boolean customField;

    /**
     * 操作符：等于、大于、小于、等
     */
    @Schema(description = "操作符：等于、大于、小于、等")
    private String operator;

}
