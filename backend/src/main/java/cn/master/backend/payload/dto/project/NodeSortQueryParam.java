package cn.master.backend.payload.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeSortQueryParam {
    private String parentId;
    private String operator;
    private long pos;
}
