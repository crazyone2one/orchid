package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiScenario;
import cn.master.backend.mapper.ApiScenarioMapper;
import cn.master.backend.service.ApiScenarioService;
import org.springframework.stereotype.Service;

/**
 * 场景 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiScenarioServiceImpl extends ServiceImpl<ApiScenarioMapper, ApiScenario> implements ApiScenarioService {

}
