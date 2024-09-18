package cn.master.backend.util;

import cn.master.backend.handler.exception.MSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
public class FileUtils {
    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotBlank(fileName) && StringUtils.contains(fileName, "." + File.separator)) {
                    throw new MSException(Translator.get("invalid_parameter"));
                }
            }
        }
    }

    public static void deleteDir(String path) throws Exception {
        File file = new File(path);
        org.apache.commons.io.FileUtils.deleteDirectory(file);
    }

    /**
     * 获取流文件
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try (OutputStream os = new FileOutputStream(file);) {
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * MultipartFile 转 File
     *
     * @param file
    =     */
    public static File multipartFileToFile(MultipartFile file) {
        if (file != null && file.getSize() > 0) {
            try (InputStream ins = file.getInputStream()) {
                validateFileName(file.getOriginalFilename());
                File toFile = new File(org.apache.commons.io.FileUtils.getTempDirectoryPath()+File.separator+ Objects.requireNonNull(file.getOriginalFilename()));
                inputStreamToFile(ins, toFile);
                return toFile;
            } catch (Exception e) {
                LogUtils.error(e);
            }
        }
        return null;
    }
}
