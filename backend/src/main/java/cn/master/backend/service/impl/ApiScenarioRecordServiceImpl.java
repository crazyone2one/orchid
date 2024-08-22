package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiScenarioRecord;
import cn.master.backend.mapper.ApiScenarioRecordMapper;
import cn.master.backend.service.ApiScenarioRecordService;
import org.springframework.stereotype.Service;

/**
 * 场景执行记录 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiScenarioRecordServiceImpl extends ServiceImpl<ApiScenarioRecordMapper, ApiScenarioRecord> implements ApiScenarioRecordService {

}
