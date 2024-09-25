package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.UserViewCondition;
import cn.master.backend.mapper.UserViewConditionMapper;
import cn.master.backend.service.UserViewConditionService;
import org.springframework.stereotype.Service;

/**
 * 用户视图条件项 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-25
 */
@Service
public class UserViewConditionServiceImpl extends ServiceImpl<UserViewConditionMapper, UserViewCondition> implements UserViewConditionService {

}
