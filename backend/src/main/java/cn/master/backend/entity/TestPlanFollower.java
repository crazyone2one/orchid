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
 * 测试计划关注人 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试计划关注人")
@Table("test_plan_follower")
public class TestPlanFollower implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试计划ID;联合主键
     */
    @Id
    @Schema(description = "测试计划ID;联合主键")
    private String testPlanId;

    /**
     * 用户ID;联合主键
     */
    @Id
    @Schema(description = "用户ID;联合主键")
    private String userId;

}
