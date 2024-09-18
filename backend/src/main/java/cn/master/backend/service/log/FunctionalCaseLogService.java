package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.FileAssociation;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.entity.FunctionalCaseAttachment;
import cn.master.backend.entity.FunctionalCaseCustomField;
import cn.master.backend.payload.dto.functional.FunctionalCaseHistoryLogDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseEditRequest;
import cn.master.backend.util.JSON;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseLogService {

    public LogDTO updateFunctionalCaseLog(FunctionalCaseEditRequest requests, List<MultipartFile> files) {
        FunctionalCaseHistoryLogDTO historyLogDTO = getOriginalValue(requests.getId());
        LogDTO dto = getUpdateLogDTO(requests.getProjectId(), requests.getId(), requests.getName(), "/functional/case/update");
        dto.setModifiedValue(JSON.toJSONBytes(requests));
        dto.setOriginalValue(JSON.toJSONBytes(historyLogDTO));
        return dto;
    }

    private static LogDTO getUpdateLogDTO(String projectId, String sourceId, String content, String path) {
        LogDTO dto = new LogDTO(
                projectId,
                null,
                sourceId,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.FUNCTIONAL_CASE,
                content);
        dto.setHistory(true);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        return dto;
    }

    private FunctionalCaseHistoryLogDTO getOriginalValue(String id) {
        FunctionalCase functionalCase = QueryChain.of(FunctionalCase.class).where(FunctionalCase::getId).eq(id).one();
        List<FunctionalCaseCustomField> customFields = QueryChain.of(FunctionalCaseCustomField.class).where(FunctionalCaseCustomField::getCaseId).eq(id).list();
        List<FunctionalCaseAttachment> caseAttachments = QueryChain.of(FunctionalCaseAttachment.class).where(FunctionalCaseAttachment::getCaseId).eq(id).list();
        List<FileAssociation> fileAssociations = QueryChain.of(FileAssociation.class).where(FileAssociation::getSourceId).eq(id).list();
        return new FunctionalCaseHistoryLogDTO(functionalCase, customFields, caseAttachments, fileAssociations);
    }
}
