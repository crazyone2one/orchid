package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.BugRelationCase;
import cn.master.backend.mapper.BugRelationCaseMapper;
import cn.master.backend.service.BugRelationCaseService;
import org.springframework.stereotype.Service;

/**
 * 用例和缺陷的关联表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class BugRelationCaseServiceImpl extends ServiceImpl<BugRelationCaseMapper, BugRelationCase> implements BugRelationCaseService {

}
