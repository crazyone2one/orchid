package cn.master.backend.payload.request.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
public class FunctionalCaseEditRequest extends FunctionalCaseAddRequest {

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "删除本地上传(富文本里)的文件id")
    private List<String> deleteFileMetaIds;

    @Schema(description = "取消关联的文件id")
    private List<String> unLinkFilesIds;
}

