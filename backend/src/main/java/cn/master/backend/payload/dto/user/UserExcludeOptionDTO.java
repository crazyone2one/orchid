package cn.master.backend.payload.dto.user;

import cn.master.backend.payload.dto.system.ExcludeOptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExcludeOptionDTO extends ExcludeOptionDTO {
    @Schema(description =  "邮箱")
    private String email;
}
