package cn.master.backend.payload.response.plan;

import cn.master.backend.payload.dto.plan.TestPlanBugCaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 08/20/2024
 **/
@Data
public class TestPlanBugPageResponse {
    @Schema(description = "缺陷ID")
    private String id;
    @Schema(description = "缺陷业务ID")
    private String num;
    @Schema(description = "缺陷标题")
    private String title;
    @Schema(description = "缺陷内容(预览)")
    private String content;
    @Schema(description = "关联用例集合")
    private List<TestPlanBugCaseDTO> relateCases;
    @Schema(description = "处理人")
    private String handleUser;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建人")
    private String createUser;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "测试计划id")
    private String testPlanId;
}
