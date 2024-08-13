package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.OperationHistory;
import cn.master.backend.mapper.OperationHistoryMapper;
import cn.master.backend.service.OperationHistoryService;
import org.springframework.stereotype.Service;

/**
 * 变更记录 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-12
 */
@Service
public class OperationHistoryServiceImpl extends ServiceImpl<OperationHistoryMapper, OperationHistory> implements OperationHistoryService {

}
