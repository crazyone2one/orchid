package cn.master.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 测试计划配置 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划配置")
@Table("test_plan_config")
public class TestPlanConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试计划ID
     */
    @Schema(description = "测试计划ID")
    private String testPlanId;

    /**
     * 是否自定更新功能用例状态
     */
    @Schema(description = "是否自定更新功能用例状态")
    private Boolean automaticStatusUpdate;

    /**
     * 是否允许重复添加用例
     */
    @Schema(description = "是否允许重复添加用例")
    private Boolean repeatCase;

    @Schema(description = "")
    private double passThreshold;

    /**
     * 不同用例之间的执行方式(串行/并行)
     */
    @Schema(description = "不同用例之间的执行方式(串行/并行)")
    private String caseRunMode;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private byte[] id;

}
