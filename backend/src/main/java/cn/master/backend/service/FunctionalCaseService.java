package cn.master.backend.service;

import cn.master.backend.payload.dto.BaseFunctionalCaseBatchDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseDetailDTO;
import cn.master.backend.payload.dto.functional.FunctionalCasePageDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseVersionDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseAddRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseDeleteRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseEditRequest;
import cn.master.backend.payload.request.functional.FunctionalCasePageRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.FunctionalCase;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 功能用例 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface FunctionalCaseService extends IService<FunctionalCase> {

    FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId, String organizationId);

    long getNextNum(String projectId);

    Long getNextOrder(String projectId);

    FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId, String userId, boolean checkDetailCount);

    FunctionalCase updateFunctionalCase(FunctionalCaseEditRequest request, List<MultipartFile> files, String userId);

    void editFollower(String functionalCaseId, String userId);

    List<FunctionalCaseVersionDTO> getFunctionalCaseVersion(String functionalCaseId);

    void deleteFunctionalCase(FunctionalCaseDeleteRequest request, String userId);

    void handDeleteFunctionalCase(List<String> ids, Boolean deleteAll, String userId, String projectId);

    Page<FunctionalCasePageDTO> getFunctionalCasePage(FunctionalCasePageRequest request, Boolean deleted, Boolean isRepeat);

    List<String> getIds(BaseFunctionalCaseBatchDTO request, String projectId, boolean deleted);

    Map<String, Long> moduleCount(FunctionalCasePageRequest request, boolean delete);
}
