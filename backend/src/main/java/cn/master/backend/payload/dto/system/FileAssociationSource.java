package cn.master.backend.payload.dto.system;

import lombok.Data;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
public class FileAssociationSource {
    private String sourceId;
    private String sourceNum;
    private String sourceName;
    private String redirectId;
}
