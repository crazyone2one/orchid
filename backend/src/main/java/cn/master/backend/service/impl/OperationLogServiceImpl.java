package cn.master.backend.service.impl;

import cn.master.backend.entity.OperationHistory;
import cn.master.backend.entity.OperationLog;
import cn.master.backend.mapper.OperationHistoryMapper;
import cn.master.backend.mapper.OperationLogMapper;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.service.OperationLogService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-12
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    private final OperationHistoryMapper operationHistoryMapper;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void add(LogDTO log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        log.setContent(subStrContent(log.getContent()));
        mapper.insert(log);
        if (log.getHistory()) {
            operationHistoryMapper.insert(getHistory(log));
        }
    }

    @Override
    public void batchAdd(List<LogDTO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        logs.forEach(item -> {
            item.setContent(subStrContent(item.getContent()));
            // 限制长度
            mapper.insert(item);
            if (item.getHistory()) {
                operationHistoryMapper.insert(getHistory(item));
            }
        });
    }

    @Override
    public void deleteBySourceIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        QueryChain<OperationLog> queryChain = queryChain().where(OperationLog::getSourceId).in(ids);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
    }

    private OperationHistory getHistory(LogDTO log) {
        OperationHistory history = new OperationHistory();
        BeanUtils.copyProperties(log, history);
        return history;
    }

    private String subStrContent(String content) {
        if (StringUtils.isNotBlank(content) && content.length() > 500) {
            return content.substring(0, 499);
        }
        return content;
    }
}
