package cn.master.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/10/2024
 **/

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = MinioProperties.MINIO_PREFIX)
public class MinioProperties {
    public static final String MINIO_PREFIX = "minio";

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String defaultBucketName;
}
