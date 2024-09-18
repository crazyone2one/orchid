package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.CaseReviewHistory;
import cn.master.backend.mapper.CaseReviewHistoryMapper;
import cn.master.backend.service.CaseReviewHistoryService;
import org.springframework.stereotype.Service;

/**
 * 评审历史表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
public class CaseReviewHistoryServiceImpl extends ServiceImpl<CaseReviewHistoryMapper, CaseReviewHistory> implements CaseReviewHistoryService {

}
