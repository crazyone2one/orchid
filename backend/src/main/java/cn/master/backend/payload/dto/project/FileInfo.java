package cn.master.backend.payload.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "是否本地")
    private Boolean local;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "文件存储方式")
    private String storage;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "文件资源ID")
    private String metadataId;

    @Schema(description = "是否删除")
    private boolean deleted;

    @Schema(description = "删除时的文件名称")
    private String deletedFileName;
}
