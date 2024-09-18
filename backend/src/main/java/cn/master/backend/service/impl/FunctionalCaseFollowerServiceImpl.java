package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FunctionalCaseFollower;
import cn.master.backend.mapper.FunctionalCaseFollowerMapper;
import cn.master.backend.service.FunctionalCaseFollowerService;
import org.springframework.stereotype.Service;

/**
 * 功能用例和关注人的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Service
public class FunctionalCaseFollowerServiceImpl extends ServiceImpl<FunctionalCaseFollowerMapper, FunctionalCaseFollower> implements FunctionalCaseFollowerService {

}
