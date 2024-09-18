package cn.master.backend.service.log;

import cn.master.backend.constants.HttpMethodConstants;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.CaseReview;
import cn.master.backend.entity.CaseReviewFunctionalCase;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.mapper.CaseReviewMapper;
import cn.master.backend.mapper.FunctionalCaseMapper;
import cn.master.backend.payload.dto.BaseFunctionalCaseBatchDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.request.functional.BaseAssociateCaseRequest;
import cn.master.backend.payload.request.functional.BaseReviewCaseBatchRequest;
import cn.master.backend.payload.request.functional.CaseReviewAssociateRequest;
import cn.master.backend.payload.request.functional.CaseReviewRequest;
import cn.master.backend.service.CaseReviewFunctionalCaseService;
import cn.master.backend.service.FunctionalCaseService;
import cn.master.backend.util.JSON;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CaseReviewLogService {
    final CaseReviewMapper caseReviewMapper;
    final FunctionalCaseService functionalCaseService;
    final CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;

    /**
     * 新增用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO addCaseReviewLog(CaseReviewRequest requests) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                OperationLogModule.CASE_REVIEW,
                requests.getName());

        dto.setPath("/case/review/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }

    /**
     * 复制用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO copyCaseReviewLog(CaseReviewRequest requests) {
        LogDTO dto = new LogDTO(
                requests.getProjectId(),
                null,
                null,
                null,
                OperationLogType.COPY.name(),
                OperationLogModule.CASE_REVIEW,
                requests.getName());

        dto.setPath("/case/review/copy");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(requests));
        return dto;
    }

    /**
     * 更新用例评审 日志
     *
     * @param requests 页面参数
     * @return LogDTO
     */
    public LogDTO updateCaseReviewLog(CaseReviewRequest requests) {
        CaseReview caseReview = caseReviewMapper.selectOneById(requests.getId());
        if (caseReview == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                caseReview.getCreateUser(),
                OperationLogType.UPDATE.name(),
                OperationLogModule.CASE_REVIEW,
                caseReview.getName());

        dto.setPath("/case/review/edit");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(caseReview));
        return dto;
    }

    /**
     * 删除用例 日志
     *
     * @param reviewId reviewId
     * @return LogDTO
     */
    public LogDTO deleteFunctionalCaseLog(String reviewId) {
        CaseReview caseReview = caseReviewMapper.selectOneById(reviewId);
        if (caseReview != null) {
            LogDTO dto = new LogDTO(
                    caseReview.getProjectId(),
                    null,
                    caseReview.getId(),
                    null,
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_REVIEW,
                    caseReview.getName());

            dto.setPath("/case/review/delete");
            dto.setMethod(HttpMethodConstants.DELETE.name());
            dto.setOriginalValue(JSON.toJSONBytes(caseReview));
            return dto;
        }
        return null;
    }

    public List<LogDTO> associateCaseLog(CaseReviewAssociateRequest request) {
        CaseReview caseReview = caseReviewMapper.selectOneById(request.getReviewId());
        if (caseReview == null) {
            return null;
        }
        List<LogDTO> dtoList = new ArrayList<>();
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        if (CollectionUtils.isEmpty(caseIds)) {
            return null;
        }
        List<FunctionalCase> functionalCases = QueryChain.of(FunctionalCase.class)
                .where(FunctionalCase::getId).in(caseIds)
                .list();
        if (CollectionUtils.isEmpty(functionalCases)) {
            return null;
        }
        for (FunctionalCase functionalCase : functionalCases) {
            LogDTO dto = new LogDTO(
                    caseReview.getProjectId(),
                    null,
                    caseReview.getId(),
                    null,
                    OperationLogType.ASSOCIATE.name(),
                    OperationLogModule.CASE_REVIEW_DETAIL,
                    functionalCase.getName());

            dto.setPath("/case/review/associate");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
            dtoList.add(dto);
        }

        return dtoList;
    }

    public LogDTO disAssociateCaseLog(String reviewId, String caseId) {
        CaseReview caseReview = caseReviewMapper.selectOneById(reviewId);
        if (caseReview == null) {
            return null;
        }
        FunctionalCase functionalCase = functionalCaseService.getById(caseId);
        if (functionalCase == null) {
            return null;
        }
        LogDTO dto = new LogDTO(
                caseReview.getProjectId(),
                null,
                caseReview.getId(),
                null,
                OperationLogType.DISASSOCIATE.name(),
                OperationLogModule.CASE_REVIEW_DETAIL,
                functionalCase.getName());

        dto.setPath("/case/review/disassociate");
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
        return dto;
    }


    public List<LogDTO> batchDisassociateCaseLog(BaseReviewCaseBatchRequest request) {
        List<String> ids = caseReviewFunctionalCaseService.doSelectIds(request);
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(id -> {
                CaseReviewFunctionalCase caseReviewFunctionalCase = caseReviewFunctionalCaseService.getById(id);
                FunctionalCase functionalCase = functionalCaseService.getById(caseReviewFunctionalCase.getCaseId());
                if (caseReviewFunctionalCase != null) {
                    LogDTO dto = new LogDTO(
                            null,
                            null,
                            caseReviewFunctionalCase.getId(),
                            null,
                            OperationLogType.DISASSOCIATE.name(),
                            OperationLogModule.CASE_REVIEW_DETAIL,
                            functionalCase.getName());
                    dto.setPath("/case/review/batch/disassociate");
                    dto.setMethod(HttpMethodConstants.POST.name());
                    dto.setOriginalValue(JSON.toJSONBytes(functionalCase));
                    dtoList.add(dto);
                }
            });
        }
        return dtoList;
    }

    public <T> List<String> doSelectIds(T dto, String projectId) {
        BaseFunctionalCaseBatchDTO request = (BaseFunctionalCaseBatchDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = functionalCaseService.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }
}
