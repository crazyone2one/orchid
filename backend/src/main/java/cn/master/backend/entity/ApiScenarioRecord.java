package cn.master.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 场景执行记录 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "场景执行记录")
@Table("api_scenario_record")
public class ApiScenarioRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private String id;

    /**
     * 报告id
     */
    @Id
    @Schema(description = "报告id")
    private String apiScenarioReportId;

    /**
     * 场景id
     */
    @Id
    @Schema(description = "场景id")
    private String apiScenarioId;

}
