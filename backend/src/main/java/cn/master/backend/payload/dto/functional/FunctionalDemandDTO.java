package cn.master.backend.payload.dto.functional;

import cn.master.backend.entity.FunctionalCaseDemand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
@NoArgsConstructor
public class FunctionalDemandDTO  extends FunctionalCaseDemand {
    @Schema(description = "同平台需求展开项")
    private List<FunctionalDemandDTO> children = new ArrayList<>();;

    public void addChild(FunctionalDemandDTO demandDTO) {
        children.add(demandDTO);
    }
}

