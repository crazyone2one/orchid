package cn.master.backend.entity;

import cn.master.backend.validation.Created;
import cn.master.backend.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.util.List;

/**
 * 文件基础信息 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件基础信息")
@Table("file_metadata")
public class FileMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @Id
    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_metadata.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_metadata.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    /**
     * 原始名（含后缀）
     */
    @Schema(description = "原始名（含后缀）")
    private String originalName;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String type;

    @Schema(description = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.size.not_blank}", groups = {Created.class})
    private Long size;

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

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "文件存储方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.storage.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.storage.length_range}", groups = {Created.class, Updated.class})
    private String storage;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;

    /**
     * 标签
     */
    @Schema(description = "标签")
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 文件所属模块
     */
    @Schema(description = "文件所属模块")
    private String moduleId;

    /**
     * 文件存储路径
     */
    @Schema(description = "文件存储路径")
    private String path;

    @Schema(description = "是否是最新版", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    /**
     * 启用/禁用;启用禁用（一般常用于jar文件）
     */
    @Schema(description = "启用/禁用;启用禁用（一般常用于jar文件）")
    private Boolean enable;

    @Schema(description = "同版本数据关联的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    /**
     * 文件版本号
     */
    @Schema(description = "文件版本号")
    private String fileVersion;

}
