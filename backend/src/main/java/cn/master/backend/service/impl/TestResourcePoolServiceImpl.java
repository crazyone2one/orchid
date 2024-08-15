package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.TestResourcePool;
import cn.master.backend.mapper.TestResourcePoolMapper;
import cn.master.backend.service.TestResourcePoolService;
import org.springframework.stereotype.Service;

/**
 * 测试资源池 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class TestResourcePoolServiceImpl extends ServiceImpl<TestResourcePoolMapper, TestResourcePool> implements TestResourcePoolService {

}
