package cn.master.backend.service;

import cn.master.backend.entity.TestPlanModule;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.request.plan.TestPlanModuleCreateRequest;
import cn.master.backend.payload.request.plan.TestPlanModuleUpdateRequest;
import cn.master.backend.payload.request.system.NodeMoveRequest;
import com.mybatisflex.core.service.IService;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * 测试计划模块 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
public interface TestPlanModuleService  {

    List<BaseTreeNode> getTree(String projectId);

    String add(TestPlanModuleCreateRequest request, String operator, String requestUrl, String requestMethod);

    void update(TestPlanModuleUpdateRequest request, String userId, String requestUrl, String requestMethod);

    void deleteModule(String deleteId, String operator, String requestUrl, String requestMethod);

    void moveNode(NodeMoveRequest request, String currentUser, String requestUrl, String requestMethod);

    Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList);
}
