package cn.master.backend.service.impl;

import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.mapper.CustomFieldOptionMapper;
import cn.master.backend.payload.request.system.CustomFieldOptionRequest;
import cn.master.backend.service.BaseCustomFieldOptionService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义字段选项 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class BaseCustomFieldOptionServiceImpl extends ServiceImpl<CustomFieldOptionMapper, CustomFieldOption> implements BaseCustomFieldOptionService {

    @Override
    public List<CustomFieldOption> getByFieldId(String id) {
        List<CustomFieldOption> options = queryChain().where(CustomFieldOption::getFieldId).eq(id).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    @Override
    public List<CustomFieldOption> getByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return List.of();
        }
        List<CustomFieldOption> options = queryChain().where(CustomFieldOption::getFieldId).in(fieldIds).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addByFieldId(String fieldId, List<CustomFieldOption> customFieldOptions) {
        if (CollectionUtils.isEmpty(customFieldOptions)) {
            return;
        }
        customFieldOptions.forEach(item -> {
            item.setFieldId(fieldId);
            item.setInternal(BooleanUtils.isTrue(item.getInternal()));
        });
        mapper.insertBatch(customFieldOptions);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> customFieldOptionRequests) {
        List<CustomFieldOption> originOptions = getByFieldId(fieldId);
        // 查询原有选项
        Map<String, CustomFieldOption> optionMap =
                originOptions.stream().collect(Collectors.toMap(CustomFieldOption::getValue, i -> i));
        // 先删除选项，再添加
        deleteByFieldId(fieldId);
        List<CustomFieldOption> customFieldOptions = customFieldOptionRequests.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            if (optionMap.get(item.getValue()) != null) {
                // 保留选项是否是内置的选项
                customFieldOption.setInternal(optionMap.get(item.getValue()).getInternal());
            } else {
                customFieldOption.setInternal(false);
            }
            customFieldOption.setFieldId(fieldId);
            return customFieldOption;
        }).toList();
        if (CollectionUtils.isNotEmpty(customFieldOptions)) {
            mapper.insertBatch(customFieldOptions);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByFieldId(String fieldId) {
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain().where(CustomFieldOption::getFieldId).eq(fieldId)));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteByFieldIds(List<String> fieldIds) {
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain().where(CustomFieldOption::getFieldId).in(fieldIds)));
    }
}
