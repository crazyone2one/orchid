package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.ApiReportLog;
import cn.master.backend.mapper.ApiReportLogMapper;
import cn.master.backend.service.ApiReportLogService;
import org.springframework.stereotype.Service;

/**
 * 接口报告过程日志 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class ApiReportLogServiceImpl extends ServiceImpl<ApiReportLogMapper, ApiReportLog> implements ApiReportLogService {

}
