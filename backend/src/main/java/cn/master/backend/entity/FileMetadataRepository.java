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
 * 存储库文件信息拓展 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "存储库文件信息拓展")
@Table("file_metadata_repository")
public class FileMetadataRepository implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "")
    private String id;

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileMetadataId;

    /**
     * 分支
     */
    @Schema(description = "分支")
    private String branch;

    /**
     * 提交ID
     */
    @Schema(description = "提交ID")
    private String commitId;

    /**
     * 提交信息
     */
    @Schema(description = "提交信息")
    private String commitMessage;

}
