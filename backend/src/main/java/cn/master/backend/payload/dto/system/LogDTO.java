package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.OperationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/12/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class LogDTO extends OperationLog {
    @Schema(description = "是否需要历史记录")
    private Boolean history = false;

    public LogDTO() {
    }

    public LogDTO(String projectId, String organizationId, String sourceId, String createUser, String type, String module, String content) {
        this.setProjectId(projectId);
        this.setOrganizationId(organizationId);
        this.setSourceId(sourceId);
        this.setCreateUser(createUser);
        this.setType(type);
        this.setModule(module);
        this.setContent(content);
    }
}
