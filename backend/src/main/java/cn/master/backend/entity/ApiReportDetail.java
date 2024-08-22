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
 * API/CASE执行结果详情 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API/CASE执行结果详情")
@Table("api_report_detail")
public class ApiReportDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 报告详情id
     */
    @Id
    @Schema(description = "报告详情id")
    private String id;

    /**
     * 接口报告id
     */
    @Schema(description = "接口报告id")
    private String reportId;

    /**
     * 各个步骤请求唯一标识
     */
    @Schema(description = "各个步骤请求唯一标识")
    private String stepId;

    /**
     * 结果状态
     */
    @Schema(description = "结果状态")
    private String status;

    /**
     * 误报编号/误报状态独有
     */
    @Schema(description = "误报编号/误报状态独有")
    private String fakeCode;

    /**
     * 请求名称
     */
    @Schema(description = "请求名称")
    private String requestName;

    /**
     * 请求耗时
     */
    @Schema(description = "请求耗时")
    private Long requestTime;

    /**
     * 请求响应码
     */
    @Schema(description = "请求响应码")
    private String code;

    /**
     * 响应内容大小
     */
    @Schema(description = "响应内容大小")
    private Long responseSize;

    /**
     * 结果内容详情
     */
    @Schema(description = "结果内容详情")
    private byte[] content;

    /**
     * 脚本标识
     */
    @Schema(description = "脚本标识")
    private String scriptIdentifier;

}
