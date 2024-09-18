package cn.master.backend.handler.file;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class FileCopyRequest extends FileRequest {
    /**
     * 复制的文件目录
     */
    private String copyFolder;

    /**
     * 复制的文件名称
     */
    private String copyfileName;
}
