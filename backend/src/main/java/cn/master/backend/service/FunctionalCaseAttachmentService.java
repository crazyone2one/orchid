package cn.master.backend.service;

import cn.master.backend.payload.dto.functional.FunctionalCaseDetailDTO;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.FunctionalCaseAttachment;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 功能用例和附件的中间表 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface FunctionalCaseAttachmentService extends IService<FunctionalCaseAttachment> {

    List<String> uploadFile(String projectId, String caseId, List<MultipartFile> files, Boolean isLocal, String userId);

    FunctionalCaseAttachment creatAttachment(String fileId, String fileName, long fileSize, String caseId, Boolean isLocal, String userId);

    void uploadMinioFile(String caseId, String projectId, List<String> uploadFileIds, String userId, String fileSource);

    void association(List<String> relateFileMetaIds, String caseId, String userId, String logUrl, String projectId);

    void getAttachmentInfo(FunctionalCaseDetailDTO functionalCaseDetailDTO);

    void deleteCaseAttachment(List<String> deleteFileMetaIds, String caseId, String projectId);

    void unAssociation(String sourceId, List<String> unLinkFilesIds, String logUrl, String userId, String projectId);
}
