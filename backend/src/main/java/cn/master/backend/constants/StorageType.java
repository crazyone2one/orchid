package cn.master.backend.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
public enum StorageType {
    MINIO, GIT, LOCAL;

    public static boolean isGit(String storage) {
        return StringUtils.equalsIgnoreCase(GIT.name(), storage);
    }
}
