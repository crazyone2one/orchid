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
 * 缺陷关注人 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缺陷关注人")
@Table("bug_follower")
public class BugFollower implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private String id;

    /**
     * 缺陷ID
     */
    @Id
    @Schema(description = "缺陷ID")
    private String bugId;

    /**
     * 关注人ID
     */
    @Id
    @Schema(description = "关注人ID")
    private String userId;

}
