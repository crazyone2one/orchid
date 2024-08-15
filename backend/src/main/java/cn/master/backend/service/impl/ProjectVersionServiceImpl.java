package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ProjectVersion;
import cn.master.backend.mapper.ProjectVersionMapper;
import cn.master.backend.service.ProjectVersionService;
import org.springframework.stereotype.Service;

/**
 * 版本管理 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectVersionServiceImpl extends ServiceImpl<ProjectVersionMapper, ProjectVersion> implements ProjectVersionService {

}
