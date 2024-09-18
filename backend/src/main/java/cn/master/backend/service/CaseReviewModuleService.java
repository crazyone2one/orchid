package cn.master.backend.service;

import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.request.functional.CaseReviewModuleCreateRequest;
import cn.master.backend.payload.request.functional.CaseReviewModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.CaseReviewModule;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * 用例评审模块 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
public interface CaseReviewModuleService {

    Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList);

    List<BaseTreeNode> getTree(String projectId);

    String add(CaseReviewModuleCreateRequest request, String userId);

    void update(CaseReviewModuleUpdateRequest request, String userId);

    void moveNode(NodeMoveRequest request, String userId);

    void deleteModule(String moduleId);
}
