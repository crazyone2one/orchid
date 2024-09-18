package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.BugComment;
import cn.master.backend.mapper.BugCommentMapper;
import cn.master.backend.service.BugCommentService;
import org.springframework.stereotype.Service;

/**
 * 缺陷评论 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
public class BugCommentServiceImpl extends ServiceImpl<BugCommentMapper, BugComment> implements BugCommentService {

}
