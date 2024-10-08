package cn.master.backend.payload.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 08/15/2024
 **/
@Data
public class ProjectPoolRequest implements Serializable {
    @Schema(description = "组织id")
    @Size(min = 0, max = 50, message = "project.organization_id.length_range")
    private String organizationId;

    @Schema(description = "项目开启的模块id集合")
    @NotEmpty
    private List<String> modulesIds;
}

