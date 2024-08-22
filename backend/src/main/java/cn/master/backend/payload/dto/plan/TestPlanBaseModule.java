package cn.master.backend.payload.dto.plan;

import lombok.Data;

/**
 * @author Created by 11's papa on 08/19/2024
 **/
@Data
public class TestPlanBaseModule {

    /**
     * 模块ID
     */
    private String id;

    /**
     * 模块路径
     */
    private String name;

    /**
     * 父级模块ID
     */
    private String parentId;
}
