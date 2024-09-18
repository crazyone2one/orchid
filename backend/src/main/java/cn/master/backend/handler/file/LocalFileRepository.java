package cn.master.backend.handler.file;

import cn.master.backend.handler.exception.MSException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Component
public class LocalFileRepository implements FileRepository{
    @Override
    public String saveFile(MultipartFile multipartFile, FileRequest request) throws Exception {
        if (Objects.isNull(multipartFile) || Objects.isNull(request) || StringUtils.isEmpty(request.getFileName()) || StringUtils.isEmpty(request.getFolder())) {
            return null;
        }
        cn.master.backend.util.FileUtils.validateFileName(request.getFolder(), request.getFileName());
        createFileDir(request);
        File file = new File(getFilePath(request));
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        return file.getPath();
    }

    private void createFileDir(FileRequest request) {
        String dir = getFileDir(request);
        File fileDir = new File(dir);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + dir);
        }
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        try (OutputStream ops = new FileOutputStream(file)) {
            ops.write(bytes);
            return file.getPath();
        }
    }

    private String getFilePath(FileRequest request) {
        cn.master.backend.util.FileUtils.validateFileName(request.getFolder(), request.getFileName());
        return StringUtils.join(getFileDir(request), "/", request.getFileName());
    }

    private String getFileDir(FileRequest request) {
        cn.master.backend.util.FileUtils.validateFileName(request.getFolder(), request.getFileName());
        return request.getFolder();
    }

    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file.getPath();
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        String path = StringUtils.join(getFilePath(request));
        File file = new File(path);
        FileUtil.deleteContents(file);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete file: " + path);
        }
    }

    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        cn.master.backend.util.FileUtils.validateFileName(request.getFolder(), request.getFileName());
        this.delete(request);
    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return new FileInputStream(getFilePath(request));
    }

    @Override
    public void downloadFile(FileRequest request, String localPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return List.of();
    }

    @Override
    public void copyFile(FileCopyRequest request) {
        throw new MSException("Not support copy file");
    }

    @Override
    public long getFileSize(FileRequest request) {
        File file = new File(getFilePath(request));
        return file.length();
    }
}
