package cn.master.backend.payload.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Data
public class UserBatchCreateResponse {
    @Schema(description = "成功创建的数据")
    List<UserCreateInfo> successList;
    @Schema(description = "邮箱异常数据")
    Map<String, String> errorEmails;
}
