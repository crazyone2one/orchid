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
 * 功能用例模块 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例模块")
@Table("functional_case_module")
public class FunctionalCaseModule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 父节点ID
     */
    @Schema(description = "父节点ID")
    private String parentId;

    /**
     * 同一节点下的顺序
     */
    @Schema(description = "同一节点下的顺序")
    private Long pos;

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
