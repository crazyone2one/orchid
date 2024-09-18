package cn.master.backend.controller.cese;

import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.entity.FileMetadata;
import cn.master.backend.handler.annotation.HasAuthorize;
import cn.master.backend.payload.dto.project.FileInformationResponse;
import cn.master.backend.payload.request.project.FileMetadataTableRequest;
import cn.master.backend.service.FileMetadataService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Tag(name = "用例管理-功能用例-附件")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attachment")
public class FunctionalCaseAttachmentController {
    private final FileMetadataService fileMetadataService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-附件-关联文件列表分页接口")
    @HasAuthorize(PermissionConstants.FUNCTIONAL_CASE_READ)
    public Page<FileInformationResponse> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }
}
