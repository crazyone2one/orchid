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
 * 接口定义详情内容 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口定义详情内容")
@Table("api_definition_blob")
public class ApiDefinitionBlob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口fk/ 一对一关系
     */
    @Id
    @Schema(description = "接口fk/ 一对一关系")
    private String id;

    /**
     * 请求内容
     */
    @Schema(description = "请求内容")
    private byte[] request;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private byte[] response;

}
