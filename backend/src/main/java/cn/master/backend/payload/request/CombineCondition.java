package cn.master.backend.payload.request;

import cn.master.backend.constants.CustomFieldType;
import cn.master.backend.handler.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Data
public class CombineCondition {

    @Schema(description = "参数名称")
    @NotNull
    private String name;

    @Schema(description = "期望值, BETWEEN,IN,NOT_IN 时为数组, 其他为单值")
    private Object value;

    @Schema(description = "是否是自定义字段")
    @NotNull
    private Boolean customField = false;

    @Schema(description = "自定义字段的类型")
    private String customFieldType;

    @Schema(description = "操作符",
            allowableValues = {"IN", "NOT_IN", "BETWEEN", "GT", "LT", "COUNT_GT", "COUNT_LT", "EQUALS", "NOT_EQUALS", "CONTAINS", "NOT_CONTAINS", "EMPTY", "NOT_EMPTY"})
    @EnumValue(enumClass = CombineConditionOperator.class)
    private String operator;

    /**
     * 是否是多选自定义字段
     * BaseMapper.xml 中调用
     * @return
     */
    public Boolean isMultipleCustomField() {
        if (BooleanUtils.isTrue(customField)) {
            return StringUtils.equalsAny(customFieldType, CustomFieldType.MULTIPLE_SELECT.name(),
                    CustomFieldType.MULTIPLE_INPUT.name(), CustomFieldType.MULTIPLE_MEMBER.name(), CustomFieldType.CHECKBOX.name());
        }
        return false;
    }

    public boolean valid() {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(operator)) {
            return false;
        }
        if (StringUtils.equalsAny(operator, CombineConditionOperator.EMPTY.name(), CombineConditionOperator.NOT_EMPTY.name())) {
            return true;
        }
        return switch (value) {
            case null -> false;
            case List valueList when CollectionUtils.isEmpty(valueList) -> false;
            case String valueStr when StringUtils.isBlank(valueStr) -> false;
            default -> true;
        };
    }

    public enum CombineConditionOperator {
        /**
         * 属于
         */
        IN,
        /**
         * 不属于
         */
        NOT_IN,
        /**
         * 区间
         */
        BETWEEN,
        /**
         * 大于
         */
        GT,
        /**
         * 小于
         */
        LT,
        /**
         * 数量大于
         */
        COUNT_GT,
        /**
         * 数量小于
         */
        COUNT_LT,
        /**
         * 等于
         */
        EQUALS,
        /**
         * 不等于
         */
        NOT_EQUALS,
        /**
         * 包含
         */
        CONTAINS,
        /**
         * 不包含
         */
        NOT_CONTAINS,
        /**
         * 为空
         */
        EMPTY,
        /**
         * 不为空
         */
        NOT_EMPTY
    }
}

