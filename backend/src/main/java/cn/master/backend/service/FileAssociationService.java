package cn.master.backend.service;

import cn.master.backend.payload.dto.project.FileInfo;
import cn.master.backend.payload.dto.system.FileLogRecord;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.FileAssociation;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 文件资源关联 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
public interface FileAssociationService extends IService<FileAssociation> {

    List<String> association(String caseId, String sourceTypeFunctionalCase, List<String> relateFileMetaIds, FileLogRecord fileLogRecord);

    List<FileInfo> getFiles(String id);

    int deleteBySourceIdAndFileIds(String sourceId, List<String> fileIds, @Validated FileLogRecord logRecord);
}
