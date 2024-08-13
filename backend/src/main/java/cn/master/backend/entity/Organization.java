package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织")
@Table("organization")
public class Organization implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组织ID
     */
    @Id
    @Schema(description = "组织ID")
    private String id;

    /**
     * 组织编号
     */
    @Schema(description = "组织编号")
    private Long num;

    /**
     * 组织名称
     */
    @Schema(description = "组织名称")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

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
    @Column(ignore = true)
    @RelationOneToOne(selfField = "createUser", targetField = "id", targetTable = "user", valueField = "name")
    private String createUserName;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;
    @Column(ignore = true)
    @RelationOneToOne(selfField = "updateUser", targetField = "id", targetTable = "user", valueField = "name")
    private String updateUserName;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Boolean deleted;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private String deleteUser;

    /**
     * 删除时间
     */
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enable;

}
