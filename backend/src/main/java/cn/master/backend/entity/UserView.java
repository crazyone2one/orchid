package cn.master.backend.entity;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 用户视图 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户视图")
@Table("user_view")
public class UserView implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "视图ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_view.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    @Schema(description = "视图名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_view.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "视图类型，例如功能用例视图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.view_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.view_type.length_range}", groups = {Created.class, Updated.class})
    private String viewType;

    @Schema(description = "视图的应用范围，一般为项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_view.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "匹配模式：AND/OR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.search_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{user_view.search_mode.length_range}", groups = {Created.class, Updated.class})
    private String searchMode;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

}
