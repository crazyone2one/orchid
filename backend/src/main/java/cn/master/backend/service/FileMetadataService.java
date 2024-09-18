package cn.master.backend.service;

import cn.master.backend.payload.dto.project.FileInformationResponse;
import cn.master.backend.payload.request.project.FileMetadataTableRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.FileMetadata;

import java.util.List;

/**
 * 文件基础信息 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
public interface FileMetadataService extends IService<FileMetadata> {

    List<FileMetadata> selectByList(List<String> fileIds);

    Page<FileInformationResponse> page(FileMetadataTableRequest request);
}
