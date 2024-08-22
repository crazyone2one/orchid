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
 * 服务集成 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务集成")
@Table("service_integration")
public class ServiceIntegration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    /**
     * 插件的ID
     */
    @Schema(description = "插件的ID")
    private String pluginId;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enable;

    /**
     * 配置内容
     */
    @Schema(description = "配置内容")
    private byte[] configuration;

    /**
     * 组织ID
     */
    @Schema(description = "组织ID")
    private String organizationId;

}
