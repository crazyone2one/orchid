package cn.master.backend.service.impl;

import cn.master.backend.constants.CaseFileSourceType;
import cn.master.backend.constants.DefaultRepositoryDir;
import cn.master.backend.constants.OperationLogModule;
import cn.master.backend.constants.StorageType;
import cn.master.backend.entity.FunctionalCaseAttachment;
import cn.master.backend.entity.User;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.FileCopyRequest;
import cn.master.backend.handler.file.FileRepository;
import cn.master.backend.handler.file.FileRequest;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.FunctionalCaseAttachmentMapper;
import cn.master.backend.payload.dto.functional.FunctionalCaseAttachmentDTO;
import cn.master.backend.payload.dto.functional.FunctionalCaseDetailDTO;
import cn.master.backend.payload.dto.project.FileInfo;
import cn.master.backend.payload.dto.system.FileLogRecord;
import cn.master.backend.service.CommonFileService;
import cn.master.backend.service.FileAssociationService;
import cn.master.backend.service.FileService;
import cn.master.backend.service.FunctionalCaseAttachmentService;
import cn.master.backend.util.FileAssociationSourceUtil;
import cn.master.backend.util.LogUtils;
import cn.master.backend.util.TempFileUtils;
import cn.master.backend.util.Translator;
import com.google.common.collect.Lists;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能用例和附件的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Service
@RequiredArgsConstructor
public class FunctionalCaseAttachmentServiceImpl extends ServiceImpl<FunctionalCaseAttachmentMapper, FunctionalCaseAttachment> implements FunctionalCaseAttachmentService {
    private final FileService fileService;
    private final CommonFileService commonFileService;
    private final FileAssociationService fileAssociationService;
    @Value("50MB")
    private DataSize maxFileSize;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public List<String> uploadFile(String projectId, String caseId, List<MultipartFile> files, Boolean isLocal, String userId) {
        LogUtils.info("开始上传附件");
        List<String> fileIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                String fileId = IDGenerator.nextStr();
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getOriginalFilename());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId) + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                if (file.getSize() > maxFileSize.toBytes()) {
                    throw new MSException(Translator.get("file.size.is.too.large"));
                }
                try {
                    fileService.upload(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException("save file error");
                }
                saveCaseAttachment(fileId, file, caseId, isLocal, userId);
                fileIds.add(fileId);
            });
        }
        LogUtils.info("附件上传结束");
        return fileIds;
    }

    private void saveCaseAttachment(String fileId, MultipartFile file, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = creatAttachment(fileId, file.getOriginalFilename(), file.getSize(), caseId, isLocal, userId);
        mapper.insertSelective(caseAttachment);
    }

    @Override
    public FunctionalCaseAttachment creatAttachment(String fileId, String fileName, long fileSize, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = new FunctionalCaseAttachment();
        caseAttachment.setCaseId(caseId);
        caseAttachment.setFileId(fileId);
        caseAttachment.setFileName(fileName);
        caseAttachment.setSize(fileSize);
        caseAttachment.setLocal(isLocal);
        caseAttachment.setCreateUser(userId);
        return caseAttachment;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void uploadMinioFile(String caseId, String projectId, List<String> uploadFileIds, String userId, String fileSource) {
        if (CollectionUtils.isEmpty(uploadFileIds)) {
            return;
        }
        List<FunctionalCaseAttachment> existFiles = queryChain().where(FunctionalCaseAttachment::getCaseId).eq(caseId)
                .and(FunctionalCaseAttachment::getFileId).in(uploadFileIds)
                .and(FunctionalCaseAttachment::getFileSource).eq(fileSource).list();
        List<String> existFileIds = existFiles.stream().map(FunctionalCaseAttachment::getFileId).distinct().toList();
        List<String> filIds = uploadFileIds.stream().filter(t -> !existFileIds.contains(t) && StringUtils.isNotBlank(t)).toList();
        if (CollectionUtils.isEmpty(filIds)) {
            return;
        }
        String functionalCaseDir = DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId);
        // 处理本地上传文件
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        // 添加文件与功能用例的关联关系
        Map<String, String> addFileMap = new HashMap<>();
        LogUtils.info("开始上传富文本里的附件");
        List<FunctionalCaseAttachment> functionalCaseAttachments = filIds.stream().map(fileId -> {
            FunctionalCaseAttachment functionalCaseAttachment = new FunctionalCaseAttachment();
            String fileName = getTempFileNameByFileId(fileId);
            functionalCaseAttachment.setId(IDGenerator.nextStr());
            functionalCaseAttachment.setCaseId(caseId);
            functionalCaseAttachment.setFileId(fileId);
            functionalCaseAttachment.setFileName(fileName);
            functionalCaseAttachment.setFileSource(fileSource);
            long fileSize = 0;
            try {
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                fileSize = defaultRepository.getFileSize(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("读取文件大小失败");
            }
            functionalCaseAttachment.setSize(fileSize);
            functionalCaseAttachment.setLocal(true);
            functionalCaseAttachment.setCreateUser(userId);
            addFileMap.put(fileId, fileName);
            return functionalCaseAttachment;
        }).toList();
        mapper.insertBatch(functionalCaseAttachments);
        // 上传文件到对象存储
        LogUtils.info("上传文件到对象存储");
        uploadFileResource(functionalCaseDir, addFileMap, projectId, caseId);
        LogUtils.info("上传富文本里的附件结束");
    }

    @Override
    public void association(List<String> relateFileMetaIds, String caseId, String userId, String logUrl, String projectId) {
        fileAssociationService.association(caseId, FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE, relateFileMetaIds, createFileLogRecord(logUrl, userId, projectId));
    }

    @Override
    public void getAttachmentInfo(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        List<FunctionalCaseAttachment> caseAttachments = queryChain().where(FunctionalCaseAttachment::getCaseId).eq(functionalCaseDetailDTO.getId())
                .and(FunctionalCaseAttachment::getFileSource).eq(CaseFileSourceType.ATTACHMENT.toString()).list();
        List<FunctionalCaseAttachmentDTO> attachments = new ArrayList<>(Lists.transform(caseAttachments, (functionalCaseAttachment) -> {
            FunctionalCaseAttachmentDTO tmpAttachment = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyProperties(functionalCaseAttachment, tmpAttachment);
            tmpAttachment.setId(functionalCaseAttachment.getFileId());
            tmpAttachment.setAssociationId(functionalCaseAttachment.getId());
            return tmpAttachment;
        }));
        //获取关联的附件信息
        List<FileInfo> files = fileAssociationService.getFiles(functionalCaseDetailDTO.getId());
        List<FunctionalCaseAttachmentDTO> filesDTOs = new ArrayList<>(Lists.transform(files, (fileInfo) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyProperties(fileInfo, attachmentDTO);
            attachmentDTO.setId(fileInfo.getFileId());
            attachmentDTO.setAssociationId(fileInfo.getId());
            if (StringUtils.isNotBlank(fileInfo.getDeletedFileName())) {
                attachmentDTO.setFileName(fileInfo.getDeletedFileName());
            }
            return attachmentDTO;
        }));
        attachments.addAll(filesDTOs);
        attachments.sort(Comparator.comparing(FunctionalCaseAttachmentDTO::getCreateTime).reversed());
        if (CollectionUtils.isNotEmpty(attachments)) {
            List<String> userList = attachments.stream().map(FunctionalCaseAttachmentDTO::getCreateUser).toList();
            List<User> users = QueryChain.of(User.class).where(User::getId).in(userList).list();
            Map<String, String> collect = users.stream().collect(Collectors.toMap(User::getId, User::getName));
            attachments.forEach(item -> {
                String userName = collect.get(item.getCreateUser());
                item.setCreateUserName(userName);
            });
        }
        functionalCaseDetailDTO.setAttachments(attachments);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteCaseAttachment(List<String> deleteFileMetaIds, String caseId, String projectId) {
        QueryChain<FunctionalCaseAttachment> queryChain = queryChain().where(FunctionalCaseAttachment::getFileId).in(deleteFileMetaIds)
                .and(FunctionalCaseAttachment::getCaseId).eq(caseId)
                .and(FunctionalCaseAttachment::getLocal).eq(true);
        List<FunctionalCaseAttachment> delAttachment = queryChain.list();
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
        deleteMinioFile(delAttachment, projectId, caseId);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void unAssociation(String sourceId, List<String> unLinkFilesIds, String logUrl, String userId, String projectId) {
        fileAssociationService.deleteBySourceIdAndFileIds(sourceId, unLinkFilesIds, createFileLogRecord(logUrl, userId, projectId));
    }

    private void deleteMinioFile(List<FunctionalCaseAttachment> files, String projectId, String caseId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getFileName());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId) + "/" + file.getFileId());
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    fileService.deleteFile(fileRequest);
                    String fileType = StringUtils.substring(file.getFileName(), file.getFileName().lastIndexOf(".") + 1);
                    if (TempFileUtils.isImage(fileType)) {
                        //删除预览图
                        fileRequest = new FileRequest();
                        fileRequest.setFileName(file.getFileName());
                        fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, caseId) + "/" + file.getFileId());
                        fileRequest.setStorage(StorageType.MINIO.name());
                        fileService.deleteFile(fileRequest);
                    }
                } catch (Exception e) {
                    throw new MSException("delete file error");
                }
            });
        }
    }

    private FileLogRecord createFileLogRecord(String logUrl, String operator, String projectId) {
        return FileLogRecord.builder()
                .logModule(OperationLogModule.FUNCTIONAL_CASE)
                .operator(operator)
                .projectId(projectId)
                .build();
    }

    /**
     * 上传用例管理相关的资源文件
     */
    private void uploadFileResource(String folder, Map<String, String> fileMap, String projectId, String caseId) {
        if (MapUtils.isEmpty(fileMap)) {
            return;
        }
        for (String fileId : fileMap.keySet()) {
            try {
                String fileName = fileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 将临时文件移动到指定文件夹
                commonFileService.moveTempFileToFolder(fileId, fileName, folder);
                // 将文件从临时目录移动到指定的图片预览目录
                commonFileService.moveTempFileToImgReviewFolder(DefaultRepositoryDir.getFunctionalCasePreviewDir(projectId, caseId), fileId, fileName);
                // 这里不删除临时文件，批量评审需要保留，copy多次文件到正式目录
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(Translator.get("file_upload_fail"), e);
            }
        }
    }

    private String getTempFileNameByFileId(String fileId) {
        return commonFileService.getTempFileNameByFileId(fileId);
    }
}
