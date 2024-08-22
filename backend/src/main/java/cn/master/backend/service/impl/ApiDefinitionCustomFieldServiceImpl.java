package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiDefinitionCustomField;
import cn.master.backend.mapper.ApiDefinitionCustomFieldMapper;
import cn.master.backend.service.ApiDefinitionCustomFieldService;
import org.springframework.stereotype.Service;

/**
 * 自定义字段接口定义关系 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiDefinitionCustomFieldServiceImpl extends ServiceImpl<ApiDefinitionCustomFieldMapper, ApiDefinitionCustomField> implements ApiDefinitionCustomFieldService {

}
