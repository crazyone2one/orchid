package cn.master.backend.payload.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCount {

    @Schema(description = "成功用例数量")
    @Builder.Default
    private Integer success = 0;
    @Schema(description = "失败用例数量")
    @Builder.Default
    private Integer error = 0;
    @Schema(description = "误报用例数量")
    @Builder.Default
    private Integer fakeError = 0;
    @Schema(description = "阻塞用例数量")
    @Builder.Default
    private Integer block = 0;
    @Schema(description = "未执行用例数量")
    @Builder.Default
    private Integer pending = 0;
}
