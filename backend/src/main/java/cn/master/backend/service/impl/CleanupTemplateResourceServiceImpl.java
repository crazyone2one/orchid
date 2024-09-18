package cn.master.backend.service.impl;

import cn.master.backend.service.BaseCustomFieldService;
import cn.master.backend.service.BaseTemplateService;
import cn.master.backend.service.CleanupProjectResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
@RequiredArgsConstructor
public class CleanupTemplateResourceServiceImpl implements CleanupProjectResourceService {
    private final BaseTemplateService baseTemplateService;
    private final BaseCustomFieldService baseCustomFieldService;
    @Override
    public void deleteResources(String projectId) {
        baseTemplateService.deleteByScopeId(projectId);
        baseCustomFieldService.deleteByScopeId(projectId);
        //todo baseStatusFlowSettingService.deleteByScopeId(projectId);
    }
}
