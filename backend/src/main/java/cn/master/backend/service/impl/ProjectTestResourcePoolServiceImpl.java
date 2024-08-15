package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ProjectTestResourcePool;
import cn.master.backend.mapper.ProjectTestResourcePoolMapper;
import cn.master.backend.service.ProjectTestResourcePoolService;
import org.springframework.stereotype.Service;

/**
 * 项目与资源池关系表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectTestResourcePoolServiceImpl extends ServiceImpl<ProjectTestResourcePoolMapper, ProjectTestResourcePool> implements ProjectTestResourcePoolService {

}
