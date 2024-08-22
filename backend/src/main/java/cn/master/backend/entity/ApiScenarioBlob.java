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
 * 场景配置信息等详情 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "场景配置信息等详情")
@Table("api_scenario_blob")
public class ApiScenarioBlob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 场景pk
     */
    @Id
    @Schema(description = "场景pk")
    private String id;

    /**
     * 场景配置信息
     */
    @Schema(description = "场景配置信息")
    private byte[] config;

}
