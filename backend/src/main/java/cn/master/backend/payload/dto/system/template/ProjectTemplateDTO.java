package cn.master.backend.payload.dto.system.template;

import cn.master.backend.entity.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/05/2024
 **/
@Getter
@Setter
public class ProjectTemplateDTO extends Template implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否是默认模板")
    private Boolean enableDefault = false;

    @Schema(description = "是否是平台自动获取模板")
    private Boolean enablePlatformDefault = false;
}

