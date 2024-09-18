package cn.master.backend.payload.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
@AllArgsConstructor
@Builder
public class FileLogRecord {
    //操作人
    @NotBlank
    private String operator;
    //log所属记录模块
    @NotBlank
    private String logModule;
    //log所属项目ID
    @NotBlank
    private String projectId;
}

