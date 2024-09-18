package cn.master.backend.service.impl;

import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.FileAssociationMapper;
import cn.master.backend.payload.dto.project.FileInfo;
import cn.master.backend.payload.dto.system.FileAssociationSource;
import cn.master.backend.payload.dto.system.FileLogRecord;
import cn.master.backend.service.FileAssociationService;
import cn.master.backend.service.FileMetadataService;
import cn.master.backend.service.log.FileAssociationLogService;
import cn.master.backend.util.FileAssociationSourceUtil;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.ApiScenarioTableDef.API_SCENARIO;
import static cn.master.backend.entity.table.BugTableDef.BUG;
import static cn.master.backend.entity.table.FileAssociationTableDef.FILE_ASSOCIATION;
import static cn.master.backend.entity.table.FileMetadataTableDef.FILE_METADATA;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;

/**
 * 文件资源关联 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Service
@RequiredArgsConstructor
public class FileAssociationServiceImpl extends ServiceImpl<FileAssociationMapper, FileAssociation> implements FileAssociationService {
    private final FileMetadataService fileMetadataService;
    private final FileAssociationLogService fileAssociationLogService;

    @Override
    public List<String> association(String sourceId, String sourceType, List<String> fileIds, @Validated FileLogRecord fileLogRecord) {
        if (fileIds.isEmpty()) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileAssociationSource source = getFileAssociationSource(sourceType, sourceId);
        validateSourceName(source);
        List<FileMetadata> fileMetadataList = fileMetadataService.selectByList(fileIds);
        List<FileAssociation> associationdList = queryChain().where(FileAssociation::getSourceId).eq(sourceId).list();
        Map<String, FileAssociation> refIdFileAssociationMap = associationdList.stream().collect(Collectors.toMap(FileAssociation::getFileRefId, item -> item));

        List<FileMetadata> addFileList = new ArrayList<>();
        for (FileMetadata fileMetadata : fileMetadataList) {
            FileAssociation fileAssociation = refIdFileAssociationMap.get(fileMetadata.getRefId());
            if (fileAssociation == null) {
                addFileList.add(fileMetadata);
            }
        }
        return createFileAssociation(addFileList, sourceId, source.getSourceName(), sourceType, fileLogRecord);
    }

    @Override
    public List<FileInfo> getFiles(String id) {
        return queryChain()
                .select(FILE_ASSOCIATION.ID, FILE_ASSOCIATION.FILE_ID)
                .select("CONCAT( file_metadata.`name`, IF(LENGTH(file_metadata.type) = 0, '', '.'), file_metadata.type ) AS fileName")
                .select(FILE_METADATA.ORIGINAL_NAME, FILE_METADATA.SIZE, FILE_METADATA.STORAGE, FILE_METADATA.PROJECT_ID,
                        FILE_METADATA.MODULE_ID, FILE_METADATA.ID.as("metadataId"))
                .select("'false' AS local")
                .select(FILE_ASSOCIATION.CREATE_TIME, FILE_ASSOCIATION.CREATE_USER, FILE_ASSOCIATION.DELETED_FILE_NAME)
                .from(FILE_ASSOCIATION)
                .leftJoin(FILE_METADATA).on(FILE_ASSOCIATION.FILE_ID.eq(FILE_METADATA.ID))
                .where(FILE_ASSOCIATION.SOURCE_ID.eq(id))
                .listAs(FileInfo.class);
    }

    @Override
    public int deleteBySourceIdAndFileIds(String sourceId, List<String> fileIds, FileLogRecord logRecord) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return 0;
        }
        QueryChain<FileAssociation> queryChain = queryChain().where(FILE_ASSOCIATION.SOURCE_ID.eq(sourceId).and(FILE_ASSOCIATION.FILE_ID.in(fileIds)));
        return deleteAndSaveLog(queryChain, logRecord);
    }

    private int deleteAndSaveLog(QueryChain<FileAssociation> queryChain, FileLogRecord logRecord) {
        List<FileAssociation> fileAssociations = queryChain.list();
        Map<String, List<String>> sourceToFileNameMap = genSourceNameFileNameMap(fileAssociations);
        int deleteCount = mapper.deleteByQuery(queryChain);
        if (MapUtils.isNotEmpty(sourceToFileNameMap)) {
            fileAssociationLogService.saveDeleteLog(sourceToFileNameMap, logRecord);
        }
        return deleteCount;
    }

    private Map<String, List<String>> genSourceNameFileNameMap(List<FileAssociation> fileAssociations) {
        Map<String, List<String>> sourceNameFileNameMap = new HashMap<>();
        Map<String, String> fileIdMap = new HashMap<>();
        for (FileAssociation fileAssociation : fileAssociations) {
            FileAssociationSource source = getFileAssociationSource(FileAssociationSourceUtil.getQuerySql(fileAssociation.getSourceType()), fileAssociation.getSourceId());
            this.validateSourceName(source);
            String fileName = null;
            if (fileIdMap.containsKey(fileAssociation.getFileId())) {
                fileName = fileIdMap.get(fileAssociation.getFileId());
            } else {
                FileMetadata fileMetadata = fileMetadataService.queryChain().where(FileMetadata::getId).eq(fileAssociation.getFileId()).one();
                if (fileMetadata != null) {
                    fileName = fileMetadata.getName();
                    fileIdMap.put(fileMetadata.getId(), fileName);
                }
            }
            if (StringUtils.isNotEmpty(fileName)) {
                if (sourceNameFileNameMap.containsKey(source.getSourceName())) {
                    sourceNameFileNameMap.get(source.getSourceName()).add(fileName);
                } else {
                    List<String> fileNameList = new ArrayList<>();
                    fileNameList.add(fileName);
                    sourceNameFileNameMap.put(source.getSourceName(), fileNameList);
                }
            }
        }
        return sourceNameFileNameMap;
    }

    private List<String> createFileAssociation(List<FileMetadata> addFileList, String sourceId, String sourceName, String sourceType, @Validated FileLogRecord logRecord) {
        FileAssociationSourceUtil.validate(sourceType);
        if (CollectionUtils.isNotEmpty(addFileList)) {
            List<FileAssociation> createFile = new ArrayList<>();
            addFileList.forEach(item -> {
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setFileId(item.getId());
                fileAssociation.setFileRefId(item.getRefId());
                fileAssociation.setSourceId(sourceId);
                fileAssociation.setSourceType(sourceType);
                fileAssociation.setCreateUser(logRecord.getOperator());
                fileAssociation.setUpdateUser(logRecord.getOperator());
                fileAssociation.setFileVersion(item.getFileVersion());
                createFile.add(fileAssociation);
            });
            mapper.insertBatch(createFile);
            fileAssociationLogService.saveBatchInsertLog(sourceName, addFileList, logRecord);
            return createFile.stream().map(FileAssociation::getId).collect(Collectors.toList());
        }
        return List.of();
    }

    private void validateSourceName(FileAssociationSource source) {
        if (Objects.isNull(source)) {
            throw new MSException(Translator.get("file.association.source.not.exist"));
        }
    }

    private FileAssociationSource getFileAssociationSource(String sourceType, String sourceId) {
        new FileAssociationSource();
        return switch (sourceType) {
            case "BUG" ->
                    QueryChain.of(Bug.class).select(BUG.ID.as("sourceId"), BUG.ID.as("redirectId"), BUG.NUM.as("sourceNum"),
                            BUG.TITLE.as("sourceName")).from(BUG).where(BUG.ID.eq(sourceId)).oneAs(FileAssociationSource.class);
            case "FUNCTIONAL_CASE" -> QueryChain.of(FunctionalCase.class).select(FUNCTIONAL_CASE.ID.as("sourceId"),
                            FUNCTIONAL_CASE.ID.as("redirectId"),
                            FUNCTIONAL_CASE.NUM.as("sourceNum"),
                            FUNCTIONAL_CASE.NAME.as("sourceName")).from(FUNCTIONAL_CASE).where(FUNCTIONAL_CASE.ID.eq(sourceId))
                    .oneAs(FileAssociationSource.class);
            case "API_SCENARIO" -> QueryChain.of(ApiScenario.class).select(API_SCENARIO.ID.as("sourceId"),
                            API_SCENARIO.ID.as("redirectId"),
                            API_SCENARIO.NUM.as("sourceNum"),
                            API_SCENARIO.NAME.as("sourceName")).from(API_SCENARIO).where(API_SCENARIO.ID.eq(sourceId))
                    .oneAs(FileAssociationSource.class);
            default -> throw new MSException(Translator.get("file.association.error.type"));
        };
    }
}
