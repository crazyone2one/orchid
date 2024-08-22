package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.CustomField;
import cn.master.backend.mapper.CustomFieldMapper;
import cn.master.backend.service.CustomFieldService;
import org.springframework.stereotype.Service;

/**
 * 自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class CustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField> implements CustomFieldService {

}
