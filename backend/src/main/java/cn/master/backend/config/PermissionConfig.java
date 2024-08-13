package cn.master.backend.config;

import cn.master.backend.payload.PermissionCache;
import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import cn.master.backend.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Slf4j
@Configuration
public class PermissionConfig {
    @Bean
    public PermissionCache permissionCache() throws Exception {
        log.info("load permission form permission.json file");
        List<PermissionDefinitionItem> permissionDefinition = null;
        Enumeration<URL> urls = this.getClass().getClassLoader().getResources("permission.json");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String content = IOUtils.toString(url.openStream(), String.valueOf(StandardCharsets.UTF_8));
            if (StringUtils.isBlank(content)) {
                continue;
            }
            List<PermissionDefinitionItem> temp = JSON.parseArray(content, PermissionDefinitionItem.class);
            if (permissionDefinition == null) {
                permissionDefinition = temp;
            } else {
                permissionDefinition.addAll(temp);
            }
        }
        PermissionCache permissionCache = new PermissionCache();
        permissionCache.setPermissionDefinition(permissionDefinition);
        return permissionCache;
    }
}
