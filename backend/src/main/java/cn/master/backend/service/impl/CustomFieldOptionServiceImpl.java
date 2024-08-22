package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.CustomFieldOption;
import cn.master.backend.mapper.CustomFieldOptionMapper;
import cn.master.backend.service.CustomFieldOptionService;
import org.springframework.stereotype.Service;

/**
 * 自定义字段选项 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class CustomFieldOptionServiceImpl extends ServiceImpl<CustomFieldOptionMapper, CustomFieldOption> implements CustomFieldOptionService {

}
