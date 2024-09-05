package cn.master.backend.service.impl;

import cn.master.backend.constants.ProjectApplicationType;
import cn.master.backend.entity.ProjectApplication;
import cn.master.backend.entity.ProjectTestResourcePool;
import cn.master.backend.entity.TestResourcePool;
import cn.master.backend.entity.TestResourcePoolOrganization;
import cn.master.backend.mapper.ProjectApplicationMapper;
import cn.master.backend.payload.request.project.ProjectApplicationRequest;
import cn.master.backend.service.ProjectApplicationService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.ProjectTestResourcePoolTableDef.PROJECT_TEST_RESOURCE_POOL;
import static cn.master.backend.entity.table.TestResourcePoolOrganizationTableDef.TEST_RESOURCE_POOL_ORGANIZATION;
import static cn.master.backend.entity.table.TestResourcePoolTableDef.TEST_RESOURCE_POOL;

/**
 * 项目应用 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-14
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {

    @Override
    public Map<String, Object> get(ProjectApplicationRequest request, List<String> types) {
        Map<String, Object> configMap = new HashMap<>();
        List<ProjectApplication> applications = queryChain().where(ProjectApplication::getProjectId).eq(request.getProjectId())
                .and(ProjectApplication::getType).in(types).list();
        if (CollectionUtils.isNotEmpty(applications)) {
            configMap = applications.stream().collect(Collectors.toMap(ProjectApplication::getType, ProjectApplication::getTypeValue));
            putResourcePool(request.getProjectId(), configMap, request.getType());
            return configMap;
        }
        putResourcePool(request.getProjectId(), configMap, request.getType());
        return configMap;
    }

    @Override
    public void putResourcePool(String projectId, Map<String, Object> configMap, String type) {
        String poolType = null;
        String moduleType = null;
        if (StringUtils.isBlank(type)) {
            return;
        }
        if ("apiTest".equals(type)) {
            poolType = ProjectApplicationType.API.API_RESOURCE_POOL_ID.name();
            moduleType = "api_test";
        }
        if (StringUtils.isNotBlank(poolType) && StringUtils.isNotBlank(moduleType)) {
            if (configMap.containsKey(poolType)) {
                //如果是适用于所有的组织
                long count;
                boolean exists = QueryChain.of(TestResourcePool.class).where(TEST_RESOURCE_POOL.ID.eq(configMap.get(poolType).toString())
                        .and(TEST_RESOURCE_POOL.ALL_ORG.eq(true))).exists();
                if (exists) {
                    count = QueryChain.of(TestResourcePool.class).select(QueryMethods.count(TEST_RESOURCE_POOL.ID))
                            .from(TEST_RESOURCE_POOL)
                            .leftJoin(PROJECT_TEST_RESOURCE_POOL).on(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID.eq(TEST_RESOURCE_POOL.ID))
                            .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(projectId)
                                    .and(TEST_RESOURCE_POOL.ID.eq(configMap.get(poolType).toString())))
                            .count();
                } else {
                    //指定组织  则需要关联组织-资源池的关系表  看看是否再全部存在
                    count = QueryChain.of(TestResourcePoolOrganization.class)
                            .select(QueryMethods.count(TEST_RESOURCE_POOL.ID))
                            .from(TEST_RESOURCE_POOL_ORGANIZATION)
                            .leftJoin(TEST_RESOURCE_POOL).on(TEST_RESOURCE_POOL.ID.eq(TEST_RESOURCE_POOL_ORGANIZATION.TEST_RESOURCE_POOL_ID))
                            .leftJoin(PROJECT_TEST_RESOURCE_POOL).on(TEST_RESOURCE_POOL.ID.eq(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID))
                            .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(projectId)
                                    .and(TEST_RESOURCE_POOL.ID.eq(configMap.get(poolType).toString()))
                                    .and(TEST_RESOURCE_POOL.ENABLE.eq(true)))
                            .count();
                }
                if (count == 0) {
                    configMap.remove(poolType);
                }
            }
            if (!configMap.containsKey(poolType)) {
                List<ProjectTestResourcePool> projectTestResourcePools = QueryChain.of(ProjectTestResourcePool.class)
                        .select(PROJECT_TEST_RESOURCE_POOL.ALL_COLUMNS).from(PROJECT_TEST_RESOURCE_POOL)
                        .leftJoin(TEST_RESOURCE_POOL).on(TEST_RESOURCE_POOL.ID.eq(PROJECT_TEST_RESOURCE_POOL.TEST_RESOURCE_POOL_ID))
                        .where(PROJECT_TEST_RESOURCE_POOL.PROJECT_ID.eq(projectId)
                                .and(TEST_RESOURCE_POOL.ENABLE.eq(true)))
                        .list();
                if (CollectionUtils.isNotEmpty(projectTestResourcePools)) {
                    projectTestResourcePools.sort(Comparator.comparing(ProjectTestResourcePool::getTestResourcePoolId));
                    configMap.put(poolType, projectTestResourcePools.getFirst().getTestResourcePoolId());
                }
            }
        }
    }

    @Override
    public ProjectApplication getByType(String projectId, String name) {
        return queryChain().where(ProjectApplication::getProjectId).eq(projectId)
                .and(ProjectApplication::getType).eq(name).one();
    }

    @Override
    public void createOrUpdateConfig(ProjectApplication application) {
        String type = application.getType();
        String projectId = application.getProjectId();
        QueryChain<ProjectApplication> queryChain = queryChain().where(ProjectApplication::getProjectId).eq(projectId)
                .and(ProjectApplication::getType).eq(type);
        if (queryChain.exists()) {
            mapper.updateByQuery(application, queryChain);
        } else {
            mapper.insert(application);
        }
    }
}
