package cn.master.backend.payload.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Data
@Schema(description =  "权限信息")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "权限ID")
    private String id;
    @Schema(description =  "权限名称")
    private String name;
    @Schema(description =  "是否启用该权限")
    private Boolean enable = false;
    @Schema(description =  "是否是企业权限")
    private Boolean license = false;
}
