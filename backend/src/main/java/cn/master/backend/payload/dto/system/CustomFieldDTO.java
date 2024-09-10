package cn.master.backend.payload.dto.system;

import cn.master.backend.entity.CustomField;
import cn.master.backend.entity.CustomFieldOption;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomFieldDTO extends CustomField {
    private List<CustomFieldOption> options;
    /**
     * 是否被模板使用
     */
    private Boolean used = false;
    /**
     * 模板中该字段是否必选
     */
    private Boolean templateRequired = false;
    /**
     * 内置字段的 key
     */
    private String internalFieldKey;
}

