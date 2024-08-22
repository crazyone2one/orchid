package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FunctionalCaseAttachment;
import cn.master.backend.mapper.FunctionalCaseAttachmentMapper;
import cn.master.backend.service.FunctionalCaseAttachmentService;
import org.springframework.stereotype.Service;

/**
 * 功能用例和附件的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
public class FunctionalCaseAttachmentServiceImpl extends ServiceImpl<FunctionalCaseAttachmentMapper, FunctionalCaseAttachment> implements FunctionalCaseAttachmentService {

}
