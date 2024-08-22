package cn.master.backend.payload.dto.plan;

import lombok.Data;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
public class TestPlanResourceExecResultDTO {
    private String testPlanId;
    private String execResult;
    private String testPlanGroupId;
}
