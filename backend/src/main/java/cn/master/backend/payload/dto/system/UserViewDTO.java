package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.UserView;
import cn.master.backend.payload.request.CombineCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserViewDTO extends UserView {
    @Schema(description = "筛选条件")
    private List<CombineCondition> conditions;
}
