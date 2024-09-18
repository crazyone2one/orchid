package cn.master.backend.handler.invoker;

import cn.master.backend.service.CleanupProjectResourceService;
import cn.master.backend.service.CreateProjectResourceService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
@Component
public class ProjectServiceInvoker {
    private final List<CreateProjectResourceService> createProjectResourceServices;
    private final List<CleanupProjectResourceService> cleanupProjectResourceServices;

    public ProjectServiceInvoker(List<CreateProjectResourceService> createProjectResourceServices,
                                 List<CleanupProjectResourceService> cleanupProjectResourceServices) {
        this.createProjectResourceServices = createProjectResourceServices;
        this.cleanupProjectResourceServices = cleanupProjectResourceServices;
    }

    public void invokeServices(String projectId) {
        for (CleanupProjectResourceService service : cleanupProjectResourceServices) {
            service.deleteResources(projectId);
        }
    }

    public void invokeCreateServices(String projectId) {
        for (CreateProjectResourceService service : createProjectResourceServices) {
            service.createResources(projectId);
        }
    }
}
