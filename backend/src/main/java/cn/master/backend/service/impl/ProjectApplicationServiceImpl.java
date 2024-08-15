package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ProjectApplication;
import cn.master.backend.mapper.ProjectApplicationMapper;
import cn.master.backend.service.ProjectApplicationService;
import org.springframework.stereotype.Service;

/**
 * 项目应用 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {

}
