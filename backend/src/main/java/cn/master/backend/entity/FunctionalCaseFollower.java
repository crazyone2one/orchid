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
 * 功能用例和关注人的中间表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能用例和关注人的中间表")
@Table("functional_case_follower")
public class FunctionalCaseFollower implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private String id;

    /**
     * 功能用例ID
     */
    @Id
    @Schema(description = "功能用例ID")
    private String caseId;

    /**
     * 关注人ID
     */
    @Id
    @Schema(description = "关注人ID")
    private String userId;

}
