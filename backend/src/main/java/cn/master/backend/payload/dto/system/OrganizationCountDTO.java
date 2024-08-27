package cn.master.backend.payload.dto.system;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 08/26/2024
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationCountDTO {

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 项目数量
     */
    private Integer projectCount;

    /**
     * 组织ID
     */
    private String id;
}
