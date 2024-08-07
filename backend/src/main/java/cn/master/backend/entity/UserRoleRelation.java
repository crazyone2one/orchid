package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 用户组关系 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_role_relation")
public class UserRoleRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户组关系ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 组ID
     */
    private String roleId;

    /**
     * 组织或项目ID
     */
    private String sourceId;

    /**
     * 记录所在的组织ID
     */
    private String organizationId;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUser;

}
