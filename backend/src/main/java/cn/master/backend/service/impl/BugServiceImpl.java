package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.Bug;
import cn.master.backend.mapper.BugMapper;
import cn.master.backend.service.BugService;
import org.springframework.stereotype.Service;

/**
 * 缺陷 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class BugServiceImpl extends ServiceImpl<BugMapper, Bug> implements BugService {

}
