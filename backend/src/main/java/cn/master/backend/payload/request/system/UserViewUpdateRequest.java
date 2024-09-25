package cn.master.backend.payload.request.system;

import cn.master.backend.payload.request.CombineSearch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserViewUpdateRequest extends CombineSearch {
    @Schema(description = "视图ID")
    @NotBlank
    private String id;
    @Schema(description = "视图名称")
    @NotBlank
    private String name;
}
