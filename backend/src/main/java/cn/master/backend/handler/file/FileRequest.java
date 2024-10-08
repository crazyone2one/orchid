package cn.master.backend.handler.file;

import cn.master.backend.payload.dto.FileMetadataRepositoryDTO;
import cn.master.backend.payload.dto.FileModuleRepositoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@Data
public class FileRequest {

    private String folder;

    // 存储类型
    private String storage;

    // 文件名称
    private String fileName;

    //Git文件信息
    private GitFileRequest gitFileRequest;

    public void setGitFileRequest(FileModuleRepositoryDTO repository, FileMetadataRepositoryDTO file) {
        gitFileRequest = new GitFileRequest(repository.getUrl(), repository.getToken(), repository.getUserName(), file.getBranch(), file.getCommitId());
    }
}

@Data
@AllArgsConstructor
class GitFileRequest {
    private String url;
    private String token;
    private String userName;
    private String branch;
    private String commitId;
}

