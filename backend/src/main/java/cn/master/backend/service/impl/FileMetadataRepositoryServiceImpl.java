package cn.master.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.master.backend.entity.FileMetadataRepository;
import cn.master.backend.mapper.FileMetadataRepositoryMapper;
import cn.master.backend.service.FileMetadataRepositoryService;
import org.springframework.stereotype.Service;

/**
 * 存储库文件信息拓展 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
public class FileMetadataRepositoryServiceImpl extends ServiceImpl<FileMetadataRepositoryMapper, FileMetadataRepository> implements FileMetadataRepositoryService {

}
