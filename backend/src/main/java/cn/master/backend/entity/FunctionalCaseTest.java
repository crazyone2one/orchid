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

/**
 * 功能用例和其他用例的中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例和其他用例的中间表")
@Table("functional_case_test")
public class FunctionalCaseTest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String caseId;

    /**
     * 其他类型用例ID
     */
    @Schema(description = "其他类型用例ID")
    private String sourceId;

    /**
     * 用例类型：接口用例/场景用例/性能用例/UI用例
     */
    @Schema(description = "用例类型：接口用例/场景用例/性能用例/UI用例")
    private String sourceType;

    /**
     * 用例所属项目
     */
    @Schema(description = "用例所属项目")
    private String projectId;

    /**
     * 用例的版本id
     */
    @Schema(description = "用例的版本id")
    private String versionId;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

}
