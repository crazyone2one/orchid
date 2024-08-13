package cn.master.backend.service;

import cn.master.backend.payload.dto.system.LogDTO;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.OperationLog;

import java.util.List;

/**
 * 操作日志 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-12
 */
public interface OperationLogService extends IService<OperationLog> {
    void add(LogDTO log);

    void batchAdd(List<LogDTO> logs);

    void deleteBySourceIds(List<String> ids);
}
