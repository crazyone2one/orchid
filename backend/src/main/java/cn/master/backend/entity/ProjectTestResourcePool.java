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
 * 项目与资源池关系表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目与资源池关系表")
@Table("project_test_resource_pool")
public class ProjectTestResourcePool implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private String id;

    /**
     * 项目ID
     */
    @Id
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 资源池ID
     */
    @Id
    @Schema(description = "资源池ID")
    private String testResourcePoolId;

}
