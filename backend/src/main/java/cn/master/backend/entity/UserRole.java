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
 * 用户组 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_role")
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组ID
     */
    @Id
    private String id;

    /**
     * 组名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否是内置用户组
     */
    private Boolean internal;

    /**
     * 所属类型 SYSTEM ORGANIZATION PROJECT
     */
    private String type;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    /**
     * 创建人(操作人）
     */
    private String createUser;

    /**
     * 应用范围
     */
    private String scopeId;

}
