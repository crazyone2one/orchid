package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FileModule;
import cn.master.backend.mapper.FileModuleMapper;
import cn.master.backend.service.FileModuleService;
import org.springframework.stereotype.Service;

/**
 * 文件管理模块 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-13
 */
@Service
public class FileModuleServiceImpl extends ServiceImpl<FileModuleMapper, FileModule> implements FileModuleService {

}
