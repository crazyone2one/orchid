package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.BugCustomField;
import cn.master.backend.mapper.BugCustomFieldMapper;
import cn.master.backend.service.BugCustomFieldService;
import org.springframework.stereotype.Service;

/**
 * 缺陷自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class BugCustomFieldServiceImpl extends ServiceImpl<BugCustomFieldMapper, BugCustomField> implements BugCustomFieldService {

}
