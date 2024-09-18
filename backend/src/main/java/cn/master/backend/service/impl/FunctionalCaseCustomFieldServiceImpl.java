package cn.master.backend.service.impl;

import cn.master.backend.entity.FunctionalCaseCustomField;
import cn.master.backend.mapper.FunctionalCaseCustomFieldMapper;
import cn.master.backend.payload.dto.functional.CaseCustomFieldDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseCustomFieldDTO;
import cn.master.backend.service.FunctionalCaseCustomFieldService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static cn.master.backend.entity.table.FunctionalCaseCustomFieldTableDef.FUNCTIONAL_CASE_CUSTOM_FIELD;

/**
 * 自定义字段功能用例关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class FunctionalCaseCustomFieldServiceImpl extends ServiceImpl<FunctionalCaseCustomFieldMapper, FunctionalCaseCustomField> implements FunctionalCaseCustomFieldService {

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveCustomField(String caseId, List<CaseCustomFieldDTO> customFields) {
        customFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
            mapper.insertSelective(customField);
        });
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateCustomField(String caseId, List<CaseCustomFieldDTO> customFields) {
        List<String> fieldIds = customFields.stream().map(CaseCustomFieldDTO::getFieldId).toList();
        List<FunctionalCaseCustomField> defaultFields = queryChain().where(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID.eq(caseId)
                .and(FUNCTIONAL_CASE_CUSTOM_FIELD.FIELD_ID.in(fieldIds))).list();
        Map<String, FunctionalCaseCustomField> collect = defaultFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, (item) -> item));
        List<CaseCustomFieldDTO> addFields = new ArrayList<>();
        List<CaseCustomFieldDTO> updateFields = new ArrayList<>();
        customFields.forEach(customField -> {
            if (collect.containsKey(customField.getFieldId())) {
                updateFields.add(customField);
            } else {
                addFields.add(customField);
            }
        });
        if (CollectionUtils.isNotEmpty(addFields)) {
            saveCustomField(caseId, addFields);
        }
        ;
        if (CollectionUtils.isNotEmpty(updateFields)) {
            updateField(caseId, updateFields);
        }
    }

    @Override
    public List<FunctionalCaseCustomFieldDTO> getCustomFieldsByCaseIds(List<String> ids) {
        return queryChain()
                .select(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID, FUNCTIONAL_CASE_CUSTOM_FIELD.FIELD_ID)
                .select(FUNCTIONAL_CASE_CUSTOM_FIELD.VALUE.as("defaultValue"))
                .select(CUSTOM_FIELD.NAME.as("fieldName"))
                .select(CUSTOM_FIELD.INTERNAL)
                .select(CUSTOM_FIELD.TYPE)
                .select(CUSTOM_FIELD.NAME.as("internalFieldKey"))
                .from(FUNCTIONAL_CASE_CUSTOM_FIELD)
                .innerJoin(CUSTOM_FIELD).on(FUNCTIONAL_CASE_CUSTOM_FIELD.FIELD_ID.eq(CUSTOM_FIELD.ID))
                .where(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID.in(ids))
                .listAs(FunctionalCaseCustomFieldDTO.class);
    }

    private void updateField(String caseId, List<CaseCustomFieldDTO> updateFields) {
        updateFields.forEach(custom -> {
            FunctionalCaseCustomField customField = new FunctionalCaseCustomField();
            customField.setCaseId(caseId);
            customField.setFieldId(custom.getFieldId());
            customField.setValue(custom.getValue());
            mapper.update(customField);
        });
    }
}
