package cn.master.backend.service.impl;

import cn.master.backend.constants.BugPlatform;
import cn.master.backend.entity.ServiceIntegration;
import cn.master.backend.payload.dto.SelectOption;
import cn.master.backend.service.BugCommonService;
import cn.master.backend.service.ProjectApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/19/2024
 **/
@Service
@RequiredArgsConstructor
public class BugCommonServiceImpl implements BugCommonService {
    private final ProjectApplicationService projectApplicationService;

    @Override
    public List<SelectOption> getHeaderHandlerOption(String projectId) {
        //todo
        return List.of();
    }

    @Override
    public List<SelectOption> getLocalHandlerOption(String projectId) {
        //todo
        return List.of();
    }

    @Override
    public Map<String, String> getAllStatusMap(String projectId) {
        //todo
        return Map.of();
    }
}
