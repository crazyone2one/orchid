package cn.master.backend.payload.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@AllArgsConstructor
public class ModuleSortCountResultDTO {
    private boolean isRefreshPos;
    private long pos;
}
