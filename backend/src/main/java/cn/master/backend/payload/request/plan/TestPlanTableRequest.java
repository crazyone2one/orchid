package cn.master.backend.payload.request.plan;

import cn.master.backend.payload.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TestPlanTableRequest extends BasePageRequest {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<String> moduleIds;

    @Schema(description = "项目ID")
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    private String projectId;

    @Schema(description = "类型", allowableValues = {"ALL", "TEST_PLAN", "GROUP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}")
    private String type;

    @Schema(description = "通过Keyword过滤出的测试子计划的测试计划组id")
    private List<String> keywordFilterIds;

    @Schema(description = "通过其他条件查询出来的，必须要包含的测试计划ID")
    private List<String> innerIds;
}
