package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.BugFollower;
import cn.master.backend.mapper.BugFollowerMapper;
import cn.master.backend.service.BugFollowerService;
import org.springframework.stereotype.Service;

/**
 * 缺陷关注人 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
public class BugFollowerServiceImpl extends ServiceImpl<BugFollowerMapper, BugFollower> implements BugFollowerService {

}
