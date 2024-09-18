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
 * 功能用例和需求的中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例和需求的中间表")
@Table("functional_case_demand")
public class FunctionalCaseDemand implements Serializable {

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
     * 父需求id
     */
    @Schema(description = "父需求id")
    private String parent;

    /**
     * 是否与父节点一起关联：0-否，1-是
     */
    @Schema(description = "是否与父节点一起关联：0-否，1-是")
    private Boolean withParent;

    /**
     * 需求ID
     */
    @Schema(description = "需求ID")
    private String demandId;

    /**
     * 需求标题
     */
    @Schema(description = "需求标题")
    private String demandName;

    /**
     * 需求地址
     */
    @Schema(description = "需求地址")
    private String demandUrl;

    /**
     * 需求所属平台
     */
    @Schema(description = "需求所属平台")
    private String demandPlatform;

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
