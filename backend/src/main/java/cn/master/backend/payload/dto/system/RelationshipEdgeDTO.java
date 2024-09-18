package cn.master.backend.payload.dto.system;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Data
public class RelationshipEdgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sourceId;
    private String targetId;
    private String graphId;


    public RelationshipEdgeDTO(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }
}
