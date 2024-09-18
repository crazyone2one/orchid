package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.CaseReviewFollower;
import cn.master.backend.mapper.CaseReviewFollowerMapper;
import cn.master.backend.service.CaseReviewFollowerService;
import org.springframework.stereotype.Service;

/**
 * 用例评审和关注人的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-12
 */
@Service
public class CaseReviewFollowerServiceImpl extends ServiceImpl<CaseReviewFollowerMapper, CaseReviewFollower> implements CaseReviewFollowerService {

}
