package cn.master.backend.service;

import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.OrganizationParameter;

/**
 * 组织参数 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
public interface BaseOrganizationParameterService extends IService<OrganizationParameter> {

    String getOrgTemplateEnableKeyByScene(String scene);

    String getValue(String orgId, String key);
}
