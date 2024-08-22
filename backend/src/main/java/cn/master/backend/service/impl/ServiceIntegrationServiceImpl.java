package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ServiceIntegration;
import cn.master.backend.mapper.ServiceIntegrationMapper;
import cn.master.backend.service.ServiceIntegrationService;
import org.springframework.stereotype.Service;

/**
 * 服务集成 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ServiceIntegrationServiceImpl extends ServiceImpl<ServiceIntegrationMapper, ServiceIntegration> implements ServiceIntegrationService {

}
