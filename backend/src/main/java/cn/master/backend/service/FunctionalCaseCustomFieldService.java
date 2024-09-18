package cn.master.backend.service;

import cn.master.backend.entity.FunctionalCaseCustomField;
import cn.master.backend.payload.dto.functional.CaseCustomFieldDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseCustomFieldDTO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 自定义字段功能用例关系 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
public interface FunctionalCaseCustomFieldService extends IService<FunctionalCaseCustomField> {

    void saveCustomField(String caseId, List<CaseCustomFieldDTO> customFields);

    void updateCustomField(String caseId, List<CaseCustomFieldDTO> customFields);

    List<FunctionalCaseCustomFieldDTO> getCustomFieldsByCaseIds(List<String> ids);
}
