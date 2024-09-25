package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.UserView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Data
public class UserViewListGroupedDTO {
    @Schema(description = "系统视图")
    private List<UserView> internalViews;
    @Schema(description = "自定义视图")
    private List<UserView> customViews;
}

