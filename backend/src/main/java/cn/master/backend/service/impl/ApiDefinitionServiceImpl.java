package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiDefinition;
import cn.master.backend.mapper.ApiDefinitionMapper;
import cn.master.backend.service.ApiDefinitionService;
import org.springframework.stereotype.Service;

/**
 * 接口定义 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiDefinitionServiceImpl extends ServiceImpl<ApiDefinitionMapper, ApiDefinition> implements ApiDefinitionService {

}
