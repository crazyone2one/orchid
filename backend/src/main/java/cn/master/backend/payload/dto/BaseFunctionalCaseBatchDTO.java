package cn.master.backend.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 09/12/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseFunctionalCaseBatchDTO extends TableBatchProcessDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;
}