package cn.master.backend.service.impl;

import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.util.LogUtils;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
public class CleanupApiResourceServiceImpl implements CleanupProjectResourceService {
    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关接口测试资源");
    }
}
