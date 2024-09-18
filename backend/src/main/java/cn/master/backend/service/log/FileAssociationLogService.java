package cn.master.backend.service.log;

import cn.master.backend.constants.OperationLogType;
import cn.master.backend.entity.FileMetadata;
import cn.master.backend.entity.Project;
import cn.master.backend.handler.aspect.OperationLogAspect;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.ProjectMapper;
import cn.master.backend.payload.dto.LogDTOBuilder;
import cn.master.backend.payload.dto.system.FileLogRecord;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.service.OperationLogService;
import cn.master.backend.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class FileAssociationLogService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveBatchInsertLog(String sourceName, List<FileMetadata> addFileList, FileLogRecord fileLogRecord) {
        List<LogDTO> list = new ArrayList<>();
        Project project = projectMapper.selectOneById(fileLogRecord.getProjectId());
        for (FileMetadata fileMetadata : addFileList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(project.getId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.ADD.name())
                    .module(fileLogRecord.getLogModule())
                    .method(OperationLogAspect.getMethod())
                    .path(OperationLogAspect.getPath())
                    .createUser(fileLogRecord.getOperator())
                    .sourceId(fileMetadata.getId())
                    .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association") + ":" + fileMetadata.getName())
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveBatchUpdateLog(String sourceName, Collection<FileMetadata> values, FileLogRecord fileLogRecord) {
        List<LogDTO> list = new ArrayList<>();
        Project project = projectMapper.selectOneById(fileLogRecord.getProjectId());
        for (FileMetadata fileMetadata : values) {
            LogDTO dto = this.genUpdateFileAssociationLogDTO(project, sourceName, fileMetadata, fileLogRecord);
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveUpdateLog(String sourceName, FileMetadata fileMetadata, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectOneById(fileLogRecord.getProjectId());
        LogDTO dto = this.genUpdateFileAssociationLogDTO(project, sourceName, fileMetadata, fileLogRecord);
        operationLogService.add(dto);
    }

    private LogDTO genUpdateFileAssociationLogDTO(Project project, String sourceName, FileMetadata fileMetadata, FileLogRecord fileLogRecord) {
        return LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(fileLogRecord.getLogModule())
                .method(OperationLogAspect.getMethod())
                .path(OperationLogAspect.getPath())
                .createUser(fileLogRecord.getOperator())
                .sourceId(fileMetadata.getId())
                .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association.update") + ":" + fileMetadata.getName())
                .build().getLogDTO();
    }

    public void saveDeleteLog(Map<String, List<String>> sourceToFileNameMap, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectOneById(fileLogRecord.getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : sourceToFileNameMap.entrySet()) {
            String sourceName = entry.getKey();
            List<String> fileNameList = entry.getValue();
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(project.getId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.DELETE.name())
                    .module(fileLogRecord.getLogModule())
                    .method(OperationLogAspect.getMethod())
                    .path(OperationLogAspect.getPath())
                    .createUser(fileLogRecord.getOperator())
                    .sourceId(IDGenerator.nextStr())
                    .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association.delete") + ":" + StringUtils.join(fileNameList, ","))
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveTransferAssociationLog(String sourceId, String fileName, String sourceName, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectOneById(fileLogRecord.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(fileLogRecord.getLogModule())
                .method(OperationLogAspect.getMethod())
                .path(OperationLogAspect.getPath())
                .createUser(fileLogRecord.getOperator())
                .sourceId(sourceId)
                .content(sourceName + StringUtils.SPACE + Translator.get("file.log.transfer.association") + ":" + fileName)
                .build().getLogDTO();

        operationLogService.add(dto);
    }
}
