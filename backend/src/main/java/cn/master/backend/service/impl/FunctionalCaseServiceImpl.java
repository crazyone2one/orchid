package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FunctionalCase;
import cn.master.backend.mapper.FunctionalCaseMapper;
import cn.master.backend.service.FunctionalCaseService;
import org.springframework.stereotype.Service;

/**
 * 功能用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class FunctionalCaseServiceImpl extends ServiceImpl<FunctionalCaseMapper, FunctionalCase> implements FunctionalCaseService {

}
