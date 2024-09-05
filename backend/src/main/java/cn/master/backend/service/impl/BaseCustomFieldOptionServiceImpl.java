package cn.master.backend.service.impl;

import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.mapper.CustomFieldOptionMapper;
import cn.master.backend.service.BaseCustomFieldOptionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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
}
