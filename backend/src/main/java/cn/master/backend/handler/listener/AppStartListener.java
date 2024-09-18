package cn.master.backend.handler.listener;

import cn.master.backend.constants.StorageType;
import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.MinioRepository;
import cn.master.backend.util.LogUtils;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 09/10/2024
 **/
@Component
public class AppStartListener implements ApplicationRunner {
    @Resource
    MinioClient minioClient;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        ((MinioRepository) FileCenter.getRepository(StorageType.MINIO)).init(minioClient);
    }
}
