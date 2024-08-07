package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 用户api key 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_key")
public class UserKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * user_key ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    private String createUser;
    @Column(ignore = true)
    @RelationOneToOne(selfField = "createUser", targetField = "name", targetTable = "user")
    private User user;

    /**
     * access key
     */
    private String accessToken;

    /**
     * refresh token
     */
    private String refreshToken;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 状态
     */
    private Boolean enable;

    /**
     * 是否永久有效
     */
    private Boolean forever;

    /**
     * 到期时间
     */
    private Long expireTime;

    private String description;

}
