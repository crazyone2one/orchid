package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.CaseReviewUser;
import cn.master.backend.mapper.CaseReviewUserMapper;
import cn.master.backend.service.CaseReviewUserService;
import org.springframework.stereotype.Service;

/**
 * 评审和评审人中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
public class CaseReviewUserServiceImpl extends ServiceImpl<CaseReviewUserMapper, CaseReviewUser> implements CaseReviewUserService {

}
