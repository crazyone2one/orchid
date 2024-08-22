package cn.master.backend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 接口定义 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口定义")
@Table("api_definition")
public class ApiDefinition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口pk
     */
    @Id
    @Schema(description = "接口pk")
    private String id;

    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String name;

    /**
     * 接口协议
     */
    @Schema(description = "接口协议")
    private String protocol;

    /**
     * http协议类型post/get/其它协议则是协议名(mqtt)
     */
    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    /**
     * http协议路径/其它协议则为空
     */
    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    /**
     * 接口状态/进行中/已完成
     */
    @Schema(description = "接口状态/进行中/已完成")
    private String status;

    /**
     * 自定义id
     */
    @Schema(description = "自定义id")
    private Long num;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String tags;

    /**
     * 自定义排序
     */
    @Schema(description = "自定义排序")
    private Long pos;

    /**
     * 项目fk
     */
    @Schema(description = "项目fk")
    private String projectId;

    /**
     * 模块fk
     */
    @Schema(description = "模块fk")
    private String moduleId;

    /**
     * 是否为最新版本 0:否，1:是
     */
    @Schema(description = "是否为最新版本 0:否，1:是")
    private Boolean latest;

    /**
     * 版本fk
     */
    @Schema(description = "版本fk")
    private String versionId;

    /**
     * 版本引用fk
     */
    @Schema(description = "版本引用fk")
    private String refId;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 修改时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private String deleteUser;

    /**
     * 删除时间
     */
    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    /**
     * 删除状态
     */
    @Schema(description = "删除状态")
    private Boolean deleted;

}
