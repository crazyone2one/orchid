package cn.master.backend.entity;

import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 用例评审归档表 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用例评审归档表")
@Table("case_review_functional_case_archive")
public class CaseReviewFunctionalCaseArchive implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private String id;

    /**
     * 用例评审ID
     */
    @Schema(description = "用例评审ID")
    private String reviewId;

    /**
     * 功能用例ID
     */
    @Schema(description = "功能用例ID")
    private String caseId;

    /**
     * 功能用例快照（JSON)
     */
    @Schema(description = "功能用例快照（JSON)")
    private byte[] content;

}
