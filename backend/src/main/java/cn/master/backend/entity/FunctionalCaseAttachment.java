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
 * 功能用例和附件的中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例和附件的中间表")
@Table("functional_case_attachment")
public class FunctionalCaseAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String caseId;

    /**
     * 文件的ID
     */
    @Schema(description = "文件的ID")
    private String fileId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件来源
     */
    @Schema(description = "文件来源")
    private String fileSource;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    private Long size;

    /**
     * 是否本地
     */
    @Schema(description = "是否本地")
    private Boolean local;

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

}
