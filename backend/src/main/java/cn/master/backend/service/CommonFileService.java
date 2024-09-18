package cn.master.backend.service;

import cn.master.backend.constants.DefaultRepositoryDir;
import cn.master.backend.constants.StorageType;
import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.FileCopyRequest;
import cn.master.backend.handler.file.FileRepository;
import cn.master.backend.handler.file.FileRequest;
import cn.master.backend.util.LogUtils;
import cn.master.backend.util.TempFileUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Service
public class CommonFileService {
    @Value("50MB")
    private DataSize maxFileSize;

    @Resource
    private FileService fileService;
    public String getTempFileNameByFileId(String fileId) {
        return getFileNameByFileId(fileId, DefaultRepositoryDir.getSystemTempDir());
    }

    private String getFileNameByFileId(String fileId, String folder) {
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        try {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(folder + "/" + fileId);
            List<String> folderFileNames = defaultRepository.getFolderFileNames(fileRequest);
            if (folderFileNames.isEmpty()) {
                return null;
            }
            String[] pathSplit = folderFileNames.getFirst().split("/");
            return pathSplit[pathSplit.length - 1];
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    public void moveTempFileToFolder(String fileId, String fileName, String folder) throws Exception {
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        // 按ID建文件夹，避免文件名重复
        FileCopyRequest fileCopyRequest = new FileCopyRequest();
        fileCopyRequest.setCopyFolder(systemTempDir + "/" + fileId);
        fileCopyRequest.setCopyfileName(fileName);
        fileCopyRequest.setFileName(fileName);
        fileCopyRequest.setFolder(folder + "/" + fileId);
        // 将文件从临时目录复制到资源目录
        defaultRepository.copyFile(fileCopyRequest);
    }

    public void moveTempFileToImgReviewFolder(String reviewFolder, String fileId, String fileName) throws Exception {
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        // 按ID建文件夹，避免文件名重复
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        String fileType = StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1);
        if (TempFileUtils.isImage(fileType)) {
            // 图片文件自动生成预览图
            byte[] file = defaultRepository.getFile(fileRequest);
            byte[] previewImg = TempFileUtils.compressPic(file);
            fileRequest.setFolder(reviewFolder + "/" + fileId);
            fileRequest.setStorage(StorageType.MINIO.toString());
            fileService.upload(previewImg, fileRequest);
        }
    }
}
