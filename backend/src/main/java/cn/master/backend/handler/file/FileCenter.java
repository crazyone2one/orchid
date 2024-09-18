package cn.master.backend.handler.file;

import cn.master.backend.constants.StorageType;
import cn.master.backend.util.CommonBeanFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by 11's papa on 09/11/2024
 **/
public class FileCenter {
    private FileCenter() {
    }

    public static FileRepository getRepository(StorageType storageType) {
        return switch (storageType) {
            case MINIO -> CommonBeanFactory.getBean(MinioRepository.class);
            case LOCAL -> CommonBeanFactory.getBean(LocalFileRepository.class);
            //case GIT -> CommonBeanFactory.getBean(GitRepository.class);
            default -> getDefaultRepository();
        };
    }

    public static FileRepository getRepository(String storage) {
        Map<String, StorageType> storageTypeMap = new HashMap<>() {{
            put(StorageType.MINIO.name(), StorageType.MINIO);
            put(StorageType.LOCAL.name(), StorageType.LOCAL);
            put(StorageType.GIT.name(), StorageType.GIT);
        }};

        return getRepository(storageTypeMap.get(storage.toUpperCase()));
    }

    public static FileRepository getDefaultRepository() {
        return CommonBeanFactory.getBean(MinioRepository.class);
    }
}
