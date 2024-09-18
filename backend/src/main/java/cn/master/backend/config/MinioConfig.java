package cn.master.backend.config;

import cn.master.backend.util.LogUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        // 创建 MinioClient 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        // 设置临时目录下文件的过期时间
        setBucketLifecycle(minioClient, minioProperties);
        setBucketLifecycleByExcel(minioClient, minioProperties);

        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getDefaultBucketName()).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getDefaultBucketName()).build());
        }
        return minioClient;
    }

    private void setBucketLifecycleByExcel(MinioClient minioClient, MinioProperties minioProperties) {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 1, null),
                        new RuleFilter("system/export/excel"),
                        "excel-file",
                        null,
                        null,
                        null));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(minioProperties.getDefaultBucketName())
                            .config(config)
                            .build());
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    private void setBucketLifecycle(MinioClient minioClient, MinioProperties minioProperties) {
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 7, null),
                        new RuleFilter("system/temp/"),
                        "temp-file",
                        null,
                        null,
                        null));
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(minioProperties.getDefaultBucketName())
                            .config(config)
                            .build());
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}
