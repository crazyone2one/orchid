package cn.master.backend.service.impl;

import cn.master.backend.constants.TemplateScene;
import cn.master.backend.entity.OrganizationParameter;
import cn.master.backend.mapper.OrganizationParameterMapper;
import cn.master.backend.service.BaseOrganizationParameterService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static cn.master.backend.constants.OrganizationParameterConstants.*;

/**
 * 组织参数 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-04
 */
@Service
public class BaseOrganizationParameterServiceImpl extends ServiceImpl<OrganizationParameterMapper, OrganizationParameter> implements BaseOrganizationParameterService {

    @Override
    public String getOrgTemplateEnableKeyByScene(String scene) {
        Map<String, String> sceneMap = new HashMap<>();
        sceneMap.put(TemplateScene.FUNCTIONAL.name(), ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.BUG.name(), ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.API.name(), ORGANIZATION_API_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.UI.name(), ORGANIZATION_UI_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.TEST_PLAN.name(), ORGANIZATION_TEST_PLAN_TEMPLATE_ENABLE_KEY);
        return sceneMap.get(scene);
    }

    @Override
    public String getValue(String orgId, String key) {
        OrganizationParameter organizationParameter = queryChain().where(OrganizationParameter::getOrganizationId).eq(orgId)
                .and(OrganizationParameter::getParamKey).eq(key).one();
        return organizationParameter == null ? null : organizationParameter.getParamValue();
    }
}
