package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiTestCase;
import cn.master.backend.mapper.ApiTestCaseMapper;
import cn.master.backend.service.ApiTestCaseService;
import org.springframework.stereotype.Service;

/**
 * 接口用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiTestCaseServiceImpl extends ServiceImpl<ApiTestCaseMapper, ApiTestCase> implements ApiTestCaseService {

}
