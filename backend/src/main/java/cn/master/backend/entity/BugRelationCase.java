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
 * 用例和缺陷的关联表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用例和缺陷的关联表")
@Table("bug_relation_case")
public class BugRelationCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 关联功能用例ID
     */
    @Schema(description = "关联功能用例ID")
    private String caseId;

    /**
     * 缺陷ID
     */
    @Schema(description = "缺陷ID")
    private String bugId;

    /**
     * 关联的用例类型;functional/api/ui/performance
     */
    @Schema(description = "关联的用例类型;functional/api/ui/performance")
    private String caseType;

    /**
     * 关联测试计划ID
     */
    @Schema(description = "关联测试计划ID")
    private String testPlanId;

    /**
     * 关联测试计划用例ID
     */
    @Schema(description = "关联测试计划用例ID")
    private String testPlanCaseId;

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
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
