package cn.master.backend.payload.request.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Data
public class FunctionalDemandBatchRequest{

    @Schema(description =  "关键字")
    private String keyword;

    @Schema(description = "取消勾选的需求ID(关联全选需求时传参)")
    List<String> excludeIds = new ArrayList<>();

    @Schema(description = "是否选择所有(关联全选需求时传参)")
    private boolean selectAll;
}

