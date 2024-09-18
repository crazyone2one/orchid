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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 文件资源关联 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件资源关联")
@Table("file_association")
public class FileAssociation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_association.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.source_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.source_type.length_range}", groups = {Created.class, Updated.class})
    private String sourceType;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(description = "文件同版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_ref_id.length_range}", groups = {Created.class, Updated.class})
    private String fileRefId;

    /**
     * 文件版本
     */
    @Schema(description = "文件版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_association.file_version.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_association.file_version.length_range}", groups = {Created.class, Updated.class})
    private String fileVersion;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;

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
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Boolean deleted;

    /**
     * 删除时的文件名称
     */
    @Schema(description = "删除时的文件名称")
    private String deletedFileName;

}
