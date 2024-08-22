package cn.master.backend.payload.dto.plan;

import cn.master.backend.entity.TestPlanReportApiCase;
import cn.master.backend.entity.TestPlanReportApiScenario;
import cn.master.backend.entity.TestPlanReportBug;
import cn.master.backend.entity.TestPlanReportFunctionCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanReportDetailCaseDTO {

    private List<TestPlanReportFunctionCase> functionCases;

    private List<TestPlanReportApiCase> apiCases;

    private List<TestPlanReportApiScenario> apiScenarios;

    private List<TestPlanReportBug> bugs;

}
