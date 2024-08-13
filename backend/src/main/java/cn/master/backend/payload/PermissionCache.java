package cn.master.backend.payload;

import cn.master.backend.payload.dto.system.PermissionDefinitionItem;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Data
public class PermissionCache {
    private List<PermissionDefinitionItem> permissionDefinition;
}
