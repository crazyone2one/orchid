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
 * 组织参数 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织参数")
@Table("organization_parameter")
public class OrganizationParameter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String organizationId;

    /**
     * 配置项
     */
    @Schema(description = "配置项")
    private String paramKey;

    /**
     * 配置值
     */
    @Schema(description = "配置值")
    private String paramValue;

}
