package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FunctionalCaseTest;
import cn.master.backend.mapper.FunctionalCaseTestMapper;
import cn.master.backend.service.FunctionalCaseTestService;
import org.springframework.stereotype.Service;

/**
 * 功能用例和其他用例的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Service
public class FunctionalCaseTestServiceImpl extends ServiceImpl<FunctionalCaseTestMapper, FunctionalCaseTest> implements FunctionalCaseTestService {

}
