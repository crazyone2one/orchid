package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.util.List;

/**
 * 测试集 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试集")
@Table("test_plan_collection")
public class TestPlanCollection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 测试计划ID
     */
    @Schema(description = "测试计划ID")
    private String testPlanId;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private String parentId;

    /**
     * 测试集名称
     */
    @Schema(description = "测试集名称")
    private String name;

    /**
     * 测试集类型(功能/接口/场景)
     */
    @Schema(description = "测试集类型(功能/接口/场景)")
    private String type;

    /**
     * 执行方式(串行/并行)
     */
    @Schema(description = "执行方式(串行/并行)")
    private String executeMethod;

    /**
     * 是否继承
     */
    @Schema(description = "是否继承")
    private Boolean extended;

    /**
     * 是否使用环境组
     */
    @Schema(description = "是否使用环境组")
    private Boolean grouped;

    /**
     * 环境ID/环境组ID
     */
    @Schema(description = "环境ID/环境组ID")
    private String environmentId;

    /**
     * 测试资源池ID
     */
    @Schema(description = "测试资源池ID")
    private String testResourcePoolId;

    /**
     * 是否失败重试
     */
    @Schema(description = "是否失败重试")
    private Boolean retryOnFail;

    /**
     * 失败重试次数
     */
    @Schema(description = "失败重试次数")
    private Integer retryTimes;

    /**
     * 失败重试间隔(单位: ms)
     */
    @Schema(description = "失败重试间隔(单位: ms)")
    private Integer retryInterval;

    /**
     * 是否失败停止
     */
    @Schema(description = "是否失败停止")
    private Boolean stopOnFail;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 自定义排序，间隔为4096
     */
    @Schema(description = "自定义排序，间隔为4096")
    private Long pos;

    @Column(ignore = true)
    @Schema(description = "测试子集")
    private List<TestPlanCollection> children;

}
