package cn.master.backend.payload.dto.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Data
public class FunctionalCaseAttachmentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID")
    private String id;

    @Schema(description = "关联ID")
    private String associationId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "文件来源:附件(ATTACHMENT)/功能用例详情(CASE_DETAIL)/用例评论(CASE_COMMENT)/评审评论(REVIEW_COMMENT)")
    private String fileSource;

    @Schema(description = "是否本地")
    private Boolean local;

    @Schema(description =  "创建人id")
    private String createUser;

    @Schema(description =  "创建人姓名")
    private String createUserName;

    @Schema(description =  "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    private boolean deleted;
}

