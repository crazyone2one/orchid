package cn.master.backend.service;

import cn.master.backend.payload.dto.SelectOption;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/19/2024
 **/
public interface BugCommonService {
    List<SelectOption> getHeaderHandlerOption(String projectId);

    List<SelectOption> getLocalHandlerOption(String projectId);

    Map<String, String> getAllStatusMap(String projectId);

    void clearAssociateResource(String projectId, List<String> bugIds);
}
