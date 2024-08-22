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
 * 缺陷 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缺陷")
@Table("bug")
public class Bug implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Integer num;

    /**
     * 缺陷标题
     */
    @Schema(description = "缺陷标题")
    private String title;

    /**
     * 处理人集合;预留字段, 后续工作台统计可能需要
     */
    @Schema(description = "处理人集合;预留字段, 后续工作台统计可能需要")
    private String handleUsers;

    /**
     * 处理人
     */
    @Schema(description = "处理人")
    private String handleUser;

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
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateUser;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private String templateId;

    /**
     * 缺陷平台
     */
    @Schema(description = "缺陷平台")
    private String platform;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String tags;

    /**
     * 第三方平台缺陷ID
     */
    @Schema(description = "第三方平台缺陷ID")
    private String platformBugId;

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
     * 删除状态
     */
    @Schema(description = "删除状态")
    private Boolean deleted;

    /**
     * 自定义排序，间隔5000
     */
    @Schema(description = "自定义排序，间隔5000")
    private Long pos;

    /**
     * 缺陷描述
     */
    @Schema(description = "缺陷描述")
    private byte[] description;

}
