package cn.master.backend.service;

import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.FileRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Service
public class FileService {
    public String upload(MultipartFile file, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
    }
    public String upload(InputStream inputStream, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(inputStream, request);
    }

    public String upload(byte[] file, FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).saveFile(file, request);
    }

    public byte[] download(FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).getFile(request);
    }

    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return FileCenter.getRepository(request.getStorage()).getFileAsStream(request);
    }

    public void deleteFile(FileRequest request) throws Exception {
        FileCenter.getRepository(request.getStorage()).delete(request);
    }
}
