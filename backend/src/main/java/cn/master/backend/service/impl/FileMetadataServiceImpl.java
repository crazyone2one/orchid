package cn.master.backend.service.impl;

import cn.master.backend.constants.ModuleConstants;
import cn.master.backend.constants.StorageType;
import cn.master.backend.entity.FileMetadata;
import cn.master.backend.entity.FileModule;
import cn.master.backend.mapper.FileMetadataMapper;
import cn.master.backend.mapper.FileModuleMapper;
import cn.master.backend.payload.dto.project.FileInformationResponse;
import cn.master.backend.payload.request.project.FileMetadataTableRequest;
import cn.master.backend.service.FileMetadataService;
import cn.master.backend.util.FileMetadataUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.FileMetadataTableDef.FILE_METADATA;

/**
 * 文件基础信息 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-11
 */
@Service
@RequiredArgsConstructor
public class FileMetadataServiceImpl extends ServiceImpl<FileMetadataMapper, FileMetadata> implements FileMetadataService {
    private final FileModuleMapper fileModuleMapper;

    @Override
    public List<FileMetadata> selectByList(List<String> fileIds) {
        return mapper.selectListByIds(fileIds);
    }

    @Override
    public Page<FileInformationResponse> page(FileMetadataTableRequest request) {
        List<FileInformationResponse> returnList = new ArrayList<>();
        FileMetadataUtils.transformRequestFileType(request);
        Page<FileMetadata> fileMetadataPage = selectByKeywordAndFileType(request);
        for (FileMetadata fileMetadata : fileMetadataPage.getRecords()) {
            FileInformationResponse fileInformationResponse = new FileInformationResponse(fileMetadata, null);
            returnList.add(fileInformationResponse);
        }
        Page<FileInformationResponse> page = new Page<>(returnList, request.getPageSize(), request.getCurrent(), returnList.size());
        initModuleName(page);
        return page;
    }

    private Page<FileMetadata> selectByKeywordAndFileType(FileMetadataTableRequest request) {
        return queryChain()
                .where(FILE_METADATA.LATEST.eq(true).and(FILE_METADATA.PROJECT_ID.eq(request.getProjectId()))
                        .and(FILE_METADATA.NAME.like(request.getKeyword()))
                        .and(FILE_METADATA.STORAGE.eq(StorageType.MINIO.name()))
                        .and(FILE_METADATA.MODULE_ID.in(request.getModuleIds()))
                        .and(FILE_METADATA.TYPE.eq(request.getFileType()))
                        .and(FILE_METADATA.REF_ID.notIn(
                                queryChain().select(FILE_METADATA.REF_ID).from(FILE_METADATA)
                                        .where(FILE_METADATA.ID.in(request.getHiddenIds()))
                                        .listAs(String.class)
                        ))
                )
                .page(Page.of(request.getCurrent(), request.getPageSize()));
    }

    private void initModuleName(Page<FileInformationResponse> page) {
        List<String> moduleIds = page.getRecords().stream().map(FileInformationResponse::getModuleId).distinct().toList();
        Map<String, String> moduleNameMap = getModuleNameMapByIds(moduleIds);
        //Map<String, String> moduleNameMap = new HashMap<>();
        for (FileInformationResponse dto : page.getRecords()) {
            if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
                dto.setModuleName(Translator.get("file.module.default.name"));
            } else {
                dto.setModuleName(moduleNameMap.get(dto.getModuleId()));
            }
        }
    }

    private Map<String, String> getModuleNameMapByIds(List<String> moduleIds) {
        if (CollectionUtils.isEmpty(moduleIds)) {
            return new HashMap<>();
        } else {
            List<FileModule> moduleList = QueryChain.of(fileModuleMapper).where(FileModule::getId).in(moduleIds).list();
            return moduleList.stream().collect(Collectors.toMap(FileModule::getId, FileModule::getName));
        }
    }
}
