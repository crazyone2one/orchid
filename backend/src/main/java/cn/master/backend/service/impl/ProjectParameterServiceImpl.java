package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ProjectParameter;
import cn.master.backend.mapper.ProjectParameterMapper;
import cn.master.backend.service.ProjectParameterService;
import org.springframework.stereotype.Service;

/**
 * 项目级参数 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectParameterServiceImpl extends ServiceImpl<ProjectParameterMapper, ProjectParameter> implements ProjectParameterService {

}
